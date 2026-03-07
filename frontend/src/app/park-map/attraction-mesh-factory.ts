import * as THREE from 'three';
import { Attraction } from '../attraction/attraction.service';
import { secureRandom } from './secure-random';

export class AttractionMeshFactory {
  createAttractionMesh(attraction: Attraction): THREE.Group {
    const group = new THREE.Group();
    const category = attraction.category;

    switch (category) {
      case 'ROLLER_COASTER':
        this.createRollerCoaster(group);
        break;
      case 'FERRIS_WHEEL':
        this.createFerrisWheel(group);
        break;
      case 'CAROUSEL':
        this.createCarousel(group);
        break;
      case 'DROP_TOWER':
        this.createDropTower(group);
        break;
      case 'HAUNTED_HOUSE':
        this.createHauntedHouse(group);
        break;
      case 'WATER_RIDE':
        this.createWaterRide(group);
        break;
      case 'BUMPER_CARS':
        this.createBumperCars(group);
        break;
      case 'TRAIN_RIDE':
        this.createTrainRide(group);
        break;
      case 'SWING_RIDE':
        this.createSwingRide(group);
        break;
      default:
        this.createDefaultMarker(group);
    }

    // add base platform
    this.addPlatform(group);

    return group;
  }

  private addPlatform(group: THREE.Group): void {
    const platform = new THREE.Mesh(
      new THREE.CylinderGeometry(5.5, 6, 0.25, 24),
      new THREE.MeshStandardMaterial({ color: 0xa0937e, roughness: 0.9 })
    );
    platform.position.y = 0.125;
    platform.receiveShadow = true;
    group.add(platform);
  }

  private makeCanvasTexture(color: string, noise = true): THREE.Texture {
    const size = 256;
    const canvas = document.createElement('canvas');
    canvas.width = canvas.height = size;
    const ctx = canvas.getContext('2d')!;

    ctx.fillStyle = color;
    ctx.fillRect(0, 0, size, size);
    if (noise) {
      const img = ctx.getImageData(0, 0, size, size);
      for (let i = 0; i < img.data.length; i += 4) {
        const v = (secureRandom() - 0.5) * 20;
        img.data[i] = Math.max(0, Math.min(255, img.data[i] + v));
        img.data[i + 1] = Math.max(0, Math.min(255, img.data[i + 1] + v));
        img.data[i + 2] = Math.max(0, Math.min(255, img.data[i + 2] + v));
      }
      ctx.putImageData(img, 0, 0);
    }
    const tex = new THREE.CanvasTexture(canvas);
    tex.wrapS = tex.wrapT = THREE.RepeatWrapping;
    tex.repeat.set(1, 1);
    (tex as any).colorSpace = THREE.SRGBColorSpace;
    return tex;
  }

  private createRollerCoaster(group: THREE.Group): void {
    const trackMat = new THREE.MeshPhysicalMaterial({
      map: this.makeCanvasTexture('#cc2222'),
      roughness: 0.5,
      metalness: 0.3,
      clearcoat: 0.3,
    });
    const supportMat = new THREE.MeshStandardMaterial({ color: 0x555555, roughness: 0.7, metalness: 0.4 });

    // main track with loop
    const curve = new THREE.CatmullRomCurve3([
      new THREE.Vector3(-6, 0.8, -3),
      new THREE.Vector3(-4, 4.0, -1.5),
      new THREE.Vector3(-2, 2.0, 1),
      new THREE.Vector3(0, 5.5, 2),
      new THREE.Vector3(2, 4.0, 0),
      new THREE.Vector3(3.5, 2.5, -1.5),
      new THREE.Vector3(5, 0.8, -2),
    ], true);

    const tubeGeometry = new THREE.TubeGeometry(curve, 250, 0.2, 12, true);
    const track = new THREE.Mesh(tubeGeometry, trackMat);
    track.castShadow = true;
    track.receiveShadow = true;
    group.add(track);

    // double rails
    const railGeo = new THREE.TubeGeometry(curve, 250, 0.07, 8, true);
    const railMat = new THREE.MeshStandardMaterial({ color: 0x888888, metalness: 0.6, roughness: 0.4 });
    const leftRail = new THREE.Mesh(railGeo, railMat);
    leftRail.position.x = -0.3;
    leftRail.castShadow = true;
    group.add(leftRail);
    const rightRail = leftRail.clone();
    rightRail.position.x = 0.3;
    group.add(rightRail);

    // supports with cross-bracing
    const supportCount = 22;
    for (let i = 0; i < supportCount; i++) {
      const t = i / supportCount;
      const p = curve.getPointAt(t);
      const h = Math.max(0.6, p.y);

      const left = new THREE.Mesh(new THREE.CylinderGeometry(0.06, 0.08, h, 6), supportMat);
      left.position.set(p.x - 0.5, h / 2, p.z);
      left.castShadow = true;
      group.add(left);

      const right = left.clone();
      right.position.set(p.x + 0.5, h / 2, p.z);
      group.add(right);

      // cross brace
      if (h > 1.2) {
        const brace = new THREE.Mesh(new THREE.BoxGeometry(1.0, 0.05, 0.05), supportMat);
        brace.position.set(p.x, h * 0.5, p.z);
        brace.rotation.z = 0.3;
        group.add(brace);
      }
    }

    // train cars
    const carColors = [0x222222, 0xcc0000, 0x0055cc, 0xffcc00];
    for (let c = 0; c < 4; c++) {
      const carMat = new THREE.MeshPhysicalMaterial({
        color: carColors[c],
        roughness: 0.3,
        metalness: 0.5,
        clearcoat: 0.4,
      });
      const car = new THREE.Mesh(new THREE.BoxGeometry(0.8, 0.35, 0.55), carMat);
      const pt = curve.getPointAt(c * 0.04);
      car.position.set(pt.x + c * 0.9, pt.y + 0.35, pt.z);
      car.castShadow = true;
      group.add(car);
    }

    // station
    const stationMat = new THREE.MeshStandardMaterial({ color: 0x8b4513, roughness: 0.85 });
    const station = new THREE.Mesh(new THREE.BoxGeometry(3, 2, 2), stationMat);
    station.position.set(-6, 1, -3);
    station.castShadow = true;
    group.add(station);

    const stationRoof = new THREE.Mesh(
      new THREE.ConeGeometry(2.2, 1, 4),
      new THREE.MeshStandardMaterial({ color: 0xcc0000, roughness: 0.7 })
    );
    stationRoof.position.set(-6, 2.5, -3);
    stationRoof.rotation.y = Math.PI / 4;
    group.add(stationRoof);
  }

  private createFerrisWheel(group: THREE.Group): void {
    const wheelMat = new THREE.MeshPhysicalMaterial({
      map: this.makeCanvasTexture('#3366aa'),
      roughness: 0.4,
      metalness: 0.7,
      clearcoat: 0.2,
    });
    const supportMat = new THREE.MeshStandardMaterial({ color: 0x444444, metalness: 0.5, roughness: 0.5 });

    // rim with inner ring
    const rim = new THREE.Mesh(new THREE.TorusGeometry(4.5, 0.2, 12, 64), wheelMat);
    rim.rotation.x = Math.PI / 2;
    rim.position.y = 6.5;
    rim.castShadow = true;
    group.add(rim);

    const innerRim = new THREE.Mesh(new THREE.TorusGeometry(3.5, 0.08, 8, 48), supportMat);
    innerRim.rotation.x = Math.PI / 2;
    innerRim.position.y = 6.5;
    group.add(innerRim);

    // spokes
    const spokes = 16;
    for (let i = 0; i < spokes; i++) {
      const angle = (i / spokes) * Math.PI * 2;
      const spoke = new THREE.Mesh(new THREE.BoxGeometry(0.06, 0.06, 4.5), supportMat);
      spoke.position.set(Math.cos(angle) * 2.25, 6.5, Math.sin(angle) * 2.25);
      spoke.rotation.y = -angle;
      spoke.castShadow = true;
      group.add(spoke);
    }

    // cabins with varied colours
    const cabinColors = [0xff6644, 0x44aa66, 0x4488cc, 0xeecc44, 0xcc44cc, 0x44cccc];
    const cabins = 12;
    for (let i = 0; i < cabins; i++) {
      const angle = (i / cabins) * Math.PI * 2;
      const cx = Math.cos(angle) * 4.3;
      const cz = Math.sin(angle) * 4.3;
      const cy = 6.5;

      // hanging bar
      const bar = new THREE.Mesh(new THREE.CylinderGeometry(0.03, 0.03, 0.8, 6), supportMat);
      bar.position.set(cx * 0.95, cy - 0.1, cz * 0.95);
      group.add(bar);

      // cabin body
      const cabinMat = new THREE.MeshPhysicalMaterial({
        color: cabinColors[i % cabinColors.length],
        roughness: 0.5,
        metalness: 0.1,
        clearcoat: 0.3,
      });
      const cabin = new THREE.Mesh(new THREE.BoxGeometry(0.9, 0.8, 0.8), cabinMat);
      cabin.position.set(cx, cy - 0.6, cz);
      cabin.lookAt(0, 6.5, 0);
      cabin.castShadow = true;
      group.add(cabin);

      // cabin roof
      const roofMat = new THREE.MeshStandardMaterial({ color: 0x333333, roughness: 0.8 });
      const cabinRoof = new THREE.Mesh(new THREE.BoxGeometry(1.0, 0.1, 0.9), roofMat);
      cabinRoof.position.set(cx, cy - 0.15, cz);
      cabinRoof.lookAt(0, 6.5, 0);
      group.add(cabinRoof);
    }

    // A-frame supports
    const leftA = new THREE.Mesh(new THREE.CylinderGeometry(0.3, 0.4, 7), supportMat);
    leftA.position.set(-1.8, 3.5, 0);
    leftA.rotation.z = 0.14;
    leftA.castShadow = true;
    group.add(leftA);

    const rightA = leftA.clone();
    rightA.position.set(1.8, 3.5, 0);
    rightA.rotation.z = -0.14;
    group.add(rightA);

    // hub
    const hub = new THREE.Mesh(new THREE.SphereGeometry(0.4, 12, 12), wheelMat);
    hub.position.y = 6.5;
    group.add(hub);

    // lights on rim
    const lightMat = new THREE.MeshStandardMaterial({
      color: 0xffee88,
      emissive: 0xffdd44,
      emissiveIntensity: 0.6,
    });
    for (let i = 0; i < 24; i++) {
      const angle = (i / 24) * Math.PI * 2;
      const bulb = new THREE.Mesh(new THREE.SphereGeometry(0.08, 6, 6), lightMat);
      bulb.position.set(Math.cos(angle) * 4.5, 6.5, Math.sin(angle) * 4.5);
      group.add(bulb);
    }
  }

  private createCarousel(group: THREE.Group): void {
    const baseMat = new THREE.MeshPhysicalMaterial({ color: 0xffc0cb, roughness: 0.6, metalness: 0.1, clearcoat: 0.2 });
    const roofMat = new THREE.MeshPhysicalMaterial({ color: 0xffdd55, roughness: 0.4, metalness: 0.1, clearcoat: 0.3 });
    const poleMat = new THREE.MeshStandardMaterial({ color: 0xdddddd, metalness: 0.5, roughness: 0.3 });

    // ornate base
    const base = new THREE.Mesh(new THREE.CylinderGeometry(3.2, 3.4, 0.6, 32), baseMat);
    base.position.y = 0.3;
    base.receiveShadow = true;
    group.add(base);

    // base trim ring
    const trimRing = new THREE.Mesh(
      new THREE.TorusGeometry(3.3, 0.08, 8, 32),
      new THREE.MeshStandardMaterial({ color: 0xffd700, metalness: 0.6, roughness: 0.3 })
    );
    trimRing.position.y = 0.6;
    trimRing.rotation.x = Math.PI / 2;
    group.add(trimRing);

    // conical roof
    const roof = new THREE.Mesh(new THREE.ConeGeometry(3.8, 2.0, 32), roofMat);
    roof.position.y = 3.8;
    roof.castShadow = true;
    group.add(roof);

    // roof finial
    const finial = new THREE.Mesh(
      new THREE.SphereGeometry(0.25, 12, 12),
      new THREE.MeshStandardMaterial({ color: 0xffd700, metalness: 0.7, roughness: 0.2 })
    );
    finial.position.y = 4.9;
    group.add(finial);

    // center pole
    const centerPole = new THREE.Mesh(new THREE.CylinderGeometry(0.2, 0.2, 3.5), poleMat);
    centerPole.position.y = 1.75;
    group.add(centerPole);

    // hanging banners
    for (let i = 0; i < 16; i++) {
      const color = i % 2 === 0 ? 0xffffff : 0xff8888;
      const stripe = new THREE.Mesh(
        new THREE.PlaneGeometry(0.5, 1.6),
        new THREE.MeshStandardMaterial({ color, side: THREE.DoubleSide })
      );
      const a = (i / 16) * Math.PI * 2;
      stripe.position.set(Math.cos(a) * 2.8, 3.4, Math.sin(a) * 2.8);
      stripe.lookAt(0, 3.4, 0);
      stripe.receiveShadow = true;
      group.add(stripe);
    }

    // horses (varied colours)
    const horseColors = [0xffffff, 0xeecc88, 0xaa8866, 0xcccccc, 0xffddaa, 0xddbbaa, 0xe8d8c8, 0xf0e0d0];
    for (let i = 0; i < 8; i++) {
      const angle = (i / 8) * Math.PI * 2;
      const horseMat = new THREE.MeshPhysicalMaterial({
        color: horseColors[i],
        roughness: 0.6,
        metalness: 0.05,
        clearcoat: 0.2,
      });

      // horse body
      const body = new THREE.Mesh(new THREE.BoxGeometry(0.5, 0.4, 0.25), horseMat);
      body.position.set(Math.cos(angle) * 2.2, 1.0, Math.sin(angle) * 2.2);
      body.castShadow = true;
      group.add(body);

      // horse head
      const head = new THREE.Mesh(new THREE.BoxGeometry(0.18, 0.25, 0.15), horseMat);
      head.position.set(
        Math.cos(angle) * 2.2 + Math.cos(angle) * 0.3,
        1.2,
        Math.sin(angle) * 2.2 + Math.sin(angle) * 0.3
      );
      group.add(head);

      // pole through horse
      const rod = new THREE.Mesh(new THREE.CylinderGeometry(0.025, 0.025, 1.8), poleMat);
      rod.position.set(Math.cos(angle) * 2.2, 1.7, Math.sin(angle) * 2.2);
      group.add(rod);
    }

    // light bulbs around rim
    const bulbMat = new THREE.MeshStandardMaterial({
      color: 0xffeeaa,
      emissive: 0xffcc44,
      emissiveIntensity: 0.5,
    });
    for (let i = 0; i < 20; i++) {
      const angle = (i / 20) * Math.PI * 2;
      const bulb = new THREE.Mesh(new THREE.SphereGeometry(0.06, 6, 6), bulbMat);
      bulb.position.set(Math.cos(angle) * 3.5, 2.85, Math.sin(angle) * 3.5);
      group.add(bulb);
    }
  }

  private createDropTower(group: THREE.Group): void {
    const towerMat = new THREE.MeshStandardMaterial({ color: 0x383838, roughness: 0.8, metalness: 0.3 });
    const seatMat = new THREE.MeshPhysicalMaterial({ color: 0xff3333, roughness: 0.4, metalness: 0.15, clearcoat: 0.3 });
    const frameMat = new THREE.MeshStandardMaterial({ color: 0x555555, metalness: 0.5, roughness: 0.5 });

    // lattice tower structure
    const tower = new THREE.Mesh(new THREE.BoxGeometry(1.2, 13, 1.2), towerMat);
    tower.position.y = 6.5;
    tower.castShadow = true;
    group.add(tower);

    // lattice cross pieces
    for (let h = 1; h < 13; h += 1.5) {
      for (let side = 0; side < 4; side++) {
        const cross = new THREE.Mesh(new THREE.BoxGeometry(1.4, 0.06, 0.06), frameMat);
        cross.position.y = h;
        cross.rotation.y = (side / 4) * Math.PI * 2;
        cross.rotation.z = 0.4;
        group.add(cross);
      }
    }

    // seat ring
    const seatRing = new THREE.Mesh(new THREE.TorusGeometry(2.0, 0.25, 8, 16), seatMat);
    seatRing.position.y = 9;
    seatRing.rotation.x = Math.PI / 2;
    seatRing.castShadow = true;
    group.add(seatRing);

    // individual seats
    for (let i = 0; i < 8; i++) {
      const angle = (i / 8) * Math.PI * 2;
      const seat = new THREE.Mesh(new THREE.BoxGeometry(0.4, 0.5, 0.3), seatMat);
      seat.position.set(Math.cos(angle) * 2.0, 8.7, Math.sin(angle) * 2.0);
      seat.lookAt(0, 8.7, 0);
      seat.castShadow = true;
      group.add(seat);
    }

    // top cap
    const top = new THREE.Mesh(new THREE.ConeGeometry(1.5, 2, 6), towerMat);
    top.position.y = 14;
    group.add(top);

    // strobe light at top
    const strobe = new THREE.Mesh(
      new THREE.SphereGeometry(0.3, 8, 8),
      new THREE.MeshStandardMaterial({
        color: 0xff0000,
        emissive: 0xff0000,
        emissiveIntensity: 1.0,
      })
    );
    strobe.position.y = 15;
    group.add(strobe);
  }

  private createHauntedHouse(group: THREE.Group): void {
    const wallMat = new THREE.MeshStandardMaterial({
      map: this.makeCanvasTexture('#3a2a20'),
      roughness: 0.95,
    });
    const roofMat = new THREE.MeshStandardMaterial({ color: 0x1a0f0a, roughness: 1 });

    // main building
    const house = new THREE.Mesh(new THREE.BoxGeometry(6.5, 5, 4.5), wallMat);
    house.position.y = 2.5;
    house.castShadow = true;
    group.add(house);

    // pitched roof
    const roof = new THREE.Mesh(new THREE.ConeGeometry(4.8, 2.5, 4), roofMat);
    roof.position.y = 6.25;
    roof.rotation.y = Math.PI / 4;
    roof.castShadow = true;
    group.add(roof);

    // tower/turret
    const turret = new THREE.Mesh(new THREE.CylinderGeometry(0.8, 0.9, 7, 6), wallMat);
    turret.position.set(2.5, 3.5, -1.5);
    turret.castShadow = true;
    group.add(turret);

    const turretRoof = new THREE.Mesh(new THREE.ConeGeometry(1.1, 1.5, 6), roofMat);
    turretRoof.position.set(2.5, 7.5, -1.5);
    group.add(turretRoof);

    // glowing green windows
    const windowMat = new THREE.MeshStandardMaterial({
      color: 0x112211,
      emissive: 0x00ff00,
      emissiveIntensity: 0.25,
    });
    const windowPositions = [
      { x: -1.5, y: 3.0, z: 2.26 },
      { x: 0, y: 3.0, z: 2.26 },
      { x: 1.5, y: 3.0, z: 2.26 },
      { x: -1.0, y: 1.5, z: 2.26 },
      { x: 1.0, y: 1.5, z: 2.26 },
    ];
    windowPositions.forEach(wp => {
      const w = new THREE.Mesh(new THREE.BoxGeometry(0.5, 0.7, 0.05), windowMat);
      w.position.set(wp.x, wp.y, wp.z);
      group.add(w);
    });

    // graveyard fence
    const fenceMat = new THREE.MeshStandardMaterial({ color: 0x2a2a2a, metalness: 0.6, roughness: 0.6 });
    for (let i = -4; i <= 4; i++) {
      const post = new THREE.Mesh(new THREE.CylinderGeometry(0.04, 0.04, 1.2, 6), fenceMat);
      post.position.set(i * 0.8, 0.6, 3.5);
      group.add(post);
    }
    const fenceRail = new THREE.Mesh(new THREE.BoxGeometry(7, 0.06, 0.06), fenceMat);
    fenceRail.position.set(0, 1.0, 3.5);
    group.add(fenceRail);

    // dead tree
    const deadTreeMat = new THREE.MeshStandardMaterial({ color: 0x3a2a1a, roughness: 1 });
    const deadTrunk = new THREE.Mesh(new THREE.CylinderGeometry(0.15, 0.25, 3), deadTreeMat);
    deadTrunk.position.set(-3.8, 1.5, 2);
    deadTrunk.rotation.z = 0.1;
    group.add(deadTrunk);

    // bare branches
    for (let b = 0; b < 4; b++) {
      const branch = new THREE.Mesh(new THREE.CylinderGeometry(0.03, 0.06, 1.2, 4), deadTreeMat);
      branch.position.set(-3.8 + (secureRandom() - 0.5) * 0.8, 2.5 + b * 0.3, 2);
      branch.rotation.z = (secureRandom() - 0.5) * 1.2;
      group.add(branch);
    }
  }

  private createWaterRide(group: THREE.Group): void {
    const stoneMat = new THREE.MeshStandardMaterial({ color: 0x707070, roughness: 0.9 });
    const waterMat = new THREE.MeshPhysicalMaterial({
      color: 0x2080cc,
      metalness: 0.12,
      roughness: 0.04,
      clearcoat: 0.5,
      transparent: true,
      opacity: 0.88,
    });
    const slideMat = new THREE.MeshPhysicalMaterial({
      color: 0xeeeeee,
      roughness: 0.4,
      metalness: 0.2,
      clearcoat: 0.5,
    });

    // splash pool
    const pool = new THREE.Mesh(new THREE.CylinderGeometry(3.5, 3.5, 0.8, 32), stoneMat);
    pool.position.y = 0.4;
    pool.receiveShadow = true;
    group.add(pool);

    // pool lip
    const lip = new THREE.Mesh(new THREE.TorusGeometry(3.3, 0.2, 8, 32), stoneMat);
    lip.position.y = 0.8;
    lip.rotation.x = Math.PI / 2;
    group.add(lip);

    // animated water
    const waterGeo = new THREE.CircleGeometry(3.0, 64);
    const water = new THREE.Mesh(waterGeo, waterMat);
    water.rotation.x = -Math.PI / 2;
    water.position.y = 0.75;
    water.name = 'lakeWater';
    group.add(water);

    // water slide (tube)
    const slideCurve = new THREE.CatmullRomCurve3([
      new THREE.Vector3(-1.5, 5.5, -2.5),
      new THREE.Vector3(-0.8, 4.0, -1.5),
      new THREE.Vector3(-0.2, 2.8, -0.5),
      new THREE.Vector3(0.3, 1.8, 0.5),
      new THREE.Vector3(1.2, 1.0, 1.5),
    ]);
    const slideGeom = new THREE.TubeGeometry(slideCurve, 80, 0.35, 12, false);
    const slide = new THREE.Mesh(slideGeom, slideMat);
    slide.castShadow = true;
    group.add(slide);

    // launch platform
    const platform = new THREE.Mesh(new THREE.BoxGeometry(2.5, 0.4, 2), stoneMat);
    platform.position.set(-1.5, 5.5, -2.5);
    platform.castShadow = true;
    group.add(platform);

    // platform railing
    const railMat = new THREE.MeshStandardMaterial({ color: 0x444444, metalness: 0.5 });
    for (let i = 0; i < 4; i++) {
      const post = new THREE.Mesh(new THREE.CylinderGeometry(0.04, 0.04, 1), railMat);
      post.position.set(
        -1.5 + (i % 2) * 2.2 - 1.1,
        6.2,
        -2.5 + Math.floor(i / 2) * 1.6 - 0.8
      );
      group.add(post);
    }

    // stairs
    for (let s = 0; s < 6; s++) {
      const step = new THREE.Mesh(new THREE.BoxGeometry(1.2, 0.2, 0.4), stoneMat);
      step.position.set(-3.5, 0.5 + s * 0.85, -2.5);
      step.castShadow = true;
      group.add(step);
    }
  }

  private createBumperCars(group: THREE.Group): void {
    const floorMat = new THREE.MeshStandardMaterial({ color: 0x2a2a2a, roughness: 0.85 });
    const carColors = [0xff4d4d, 0x4dff88, 0x4da6ff, 0xffe34d, 0xff79ff];

    // arena floor with pattern
    const floor = new THREE.Mesh(new THREE.CylinderGeometry(4.5, 4.5, 0.25, 32), floorMat);
    floor.position.y = 0.125;
    floor.receiveShadow = true;
    group.add(floor);

    // floor pattern rings
    const ringMat = new THREE.MeshStandardMaterial({ color: 0x3a3a3a, roughness: 0.8 });
    for (const r of [2, 3.5]) {
      const ring = new THREE.Mesh(new THREE.TorusGeometry(r, 0.08, 6, 32), ringMat);
      ring.position.y = 0.26;
      ring.rotation.x = Math.PI / 2;
      group.add(ring);
    }

    // bumper cars
    for (let i = 0; i < 5; i++) {
      const angle = (i / 5) * Math.PI * 2 + secureRandom() * 0.5;
      const dist = 1.5 + secureRandom() * 1.5;
      const carMat = new THREE.MeshPhysicalMaterial({
        color: carColors[i],
        roughness: 0.3,
        metalness: 0.2,
        clearcoat: 0.4,
      });

      // car body
      const car = new THREE.Mesh(new THREE.SphereGeometry(0.65, 12, 12), carMat);
      car.position.set(Math.cos(angle) * dist, 0.5, Math.sin(angle) * dist);
      car.scale.set(1, 0.55, 1);
      car.castShadow = true;
      group.add(car);

      // bumper ring
      const bumper = new THREE.Mesh(
        new THREE.TorusGeometry(0.65, 0.06, 6, 16),
        new THREE.MeshStandardMaterial({ color: 0x222222, roughness: 0.8 })
      );
      bumper.position.set(Math.cos(angle) * dist, 0.35, Math.sin(angle) * dist);
      bumper.rotation.x = Math.PI / 2;
      group.add(bumper);

      // antenna pole
      const pole = new THREE.Mesh(new THREE.CylinderGeometry(0.02, 0.02, 1.5, 4), new THREE.MeshStandardMaterial({ color: 0x888888 }));
      pole.position.set(Math.cos(angle) * dist, 1.2, Math.sin(angle) * dist);
      group.add(pole);
    }

    // arena fence
    const fence = new THREE.Mesh(new THREE.TorusGeometry(4.7, 0.1, 8, 64), new THREE.MeshStandardMaterial({ color: 0x666666, metalness: 0.4 }));
    fence.position.y = 0.9;
    fence.rotation.x = Math.PI / 2;
    group.add(fence);

    // overhead grid structure
    const gridMat = new THREE.MeshStandardMaterial({ color: 0x555555, metalness: 0.5 });
    const gridH = 3.5;
    for (let i = 0; i < 4; i++) {
      const angle = (i / 4) * Math.PI * 2;
      const pillar = new THREE.Mesh(new THREE.CylinderGeometry(0.12, 0.15, gridH, 6), gridMat);
      pillar.position.set(Math.cos(angle) * 4.5, gridH / 2, Math.sin(angle) * 4.5);
      pillar.castShadow = true;
      group.add(pillar);
    }

    // overhead disc (grid)
    const overhead = new THREE.Mesh(
      new THREE.CylinderGeometry(4.5, 4.5, 0.1, 32),
      new THREE.MeshStandardMaterial({ color: 0x444444, roughness: 0.8, metalness: 0.3 })
    );
    overhead.position.y = gridH;
    group.add(overhead);
  }

  private createTrainRide(group: THREE.Group): void {
    const trackMat = new THREE.MeshStandardMaterial({ color: 0x6b3f1f, roughness: 0.85 });
    const metalMat = new THREE.MeshStandardMaterial({ color: 0x444444, metalness: 0.5, roughness: 0.5 });

    // track oval
    const trackCurve = new THREE.EllipseCurve(0, 0, 4.5, 3.3);
    const trackPoints = trackCurve.getPoints(200);

    // rails as thin tubes
    const pts3 = trackPoints.map(p => new THREE.Vector3(p.x, 0.1, p.y));
    const curve3d = new THREE.CatmullRomCurve3(pts3, true);
    const railGeo = new THREE.TubeGeometry(curve3d, 200, 0.04, 6, true);
    const rail = new THREE.Mesh(railGeo, metalMat);
    group.add(rail);

    // sleepers
    for (let i = 0; i < 40; i++) {
      const t = i / 40;
      const p = curve3d.getPointAt(t);
      const tangent = curve3d.getTangentAt(t);
      const sleeper = new THREE.Mesh(new THREE.BoxGeometry(0.8, 0.06, 0.15), trackMat);
      sleeper.position.set(p.x, 0.05, p.z);
      sleeper.rotation.y = Math.atan2(tangent.x, tangent.z);
      group.add(sleeper);
    }

    // locomotive (detailed)
    const engineMat = new THREE.MeshPhysicalMaterial({ color: 0x1a6b1a, roughness: 0.4, metalness: 0.2, clearcoat: 0.3 });
    const boiler = new THREE.Mesh(new THREE.CylinderGeometry(0.4, 0.4, 1.2, 12), engineMat);
    boiler.position.set(4.5, 0.6, 0);
    boiler.rotation.z = Math.PI / 2;
    boiler.castShadow = true;
    group.add(boiler);

    const cab = new THREE.Mesh(new THREE.BoxGeometry(0.8, 0.8, 0.8), engineMat);
    cab.position.set(3.8, 0.65, 0);
    cab.castShadow = true;
    group.add(cab);

    // chimney
    const chimney = new THREE.Mesh(
      new THREE.CylinderGeometry(0.1, 0.14, 0.5),
      new THREE.MeshStandardMaterial({ color: 0x111111 })
    );
    chimney.position.set(5.0, 1.1, 0);
    group.add(chimney);

    // smoke puff
    const smokeMat = new THREE.MeshBasicMaterial({ color: 0xcccccc, transparent: true, opacity: 0.4 });
    for (let s = 0; s < 3; s++) {
      const puff = new THREE.Mesh(new THREE.SphereGeometry(0.15 + s * 0.1, 6, 6), smokeMat);
      puff.position.set(5.0, 1.5 + s * 0.35, (secureRandom() - 0.5) * 0.3);
      group.add(puff);
    }

    // passenger cars
    const carColors = [0xcc3333, 0x3366cc, 0xcc9933];
    for (let i = 0; i < 3; i++) {
      const carMat = new THREE.MeshPhysicalMaterial({ color: carColors[i], roughness: 0.5, clearcoat: 0.2 });
      const car = new THREE.Mesh(new THREE.BoxGeometry(0.9, 0.55, 0.65), carMat);
      car.position.set(3.8 - (i + 1) * 1.1, 0.5, 0);
      car.castShadow = true;
      group.add(car);

      // wheels
      const wheelMat = new THREE.MeshStandardMaterial({ color: 0x222222 });
      for (const z of [-0.35, 0.35]) {
        const wheel = new THREE.Mesh(new THREE.CylinderGeometry(0.12, 0.12, 0.06, 8), wheelMat);
        wheel.position.set(3.8 - (i + 1) * 1.1, 0.2, z);
        wheel.rotation.x = Math.PI / 2;
        group.add(wheel);
      }
    }
  }

  private createSwingRide(group: THREE.Group): void {
    const poleMat = new THREE.MeshStandardMaterial({ color: 0xffd700, metalness: 0.4, roughness: 0.3 });
    const topMat = new THREE.MeshPhysicalMaterial({ color: 0xff5555, roughness: 0.5, clearcoat: 0.2 });
    const chainMat = new THREE.MeshStandardMaterial({ color: 0x999999, metalness: 0.5 });

    // center pole
    const centerPole = new THREE.Mesh(new THREE.CylinderGeometry(0.3, 0.4, 9), poleMat);
    centerPole.position.y = 4.5;
    centerPole.castShadow = true;
    group.add(centerPole);

    // decorative top disc
    const topDisc = new THREE.Mesh(new THREE.CylinderGeometry(3.8, 3.8, 1.0, 24), topMat);
    topDisc.position.y = 8.8;
    topDisc.castShadow = true;
    group.add(topDisc);

    // top trim
    const trimMat = new THREE.MeshStandardMaterial({ color: 0xffd700, metalness: 0.5, roughness: 0.3 });
    const topTrim = new THREE.Mesh(new THREE.TorusGeometry(3.8, 0.1, 8, 24), trimMat);
    topTrim.position.y = 9.3;
    topTrim.rotation.x = Math.PI / 2;
    group.add(topTrim);

    // hanging swings
    const seatColors = [0x4466ff, 0xff4444, 0x44cc44, 0xffcc00, 0xff66cc,
      0x44cccc, 0xcc8844, 0x8844cc, 0xff8844, 0x44ff88];
    for (let i = 0; i < 10; i++) {
      const angle = (i / 10) * Math.PI * 2;
      const swingAngle = 0.25; // tilted outward

      // chain
      const chain = new THREE.Mesh(new THREE.CylinderGeometry(0.02, 0.02, 3.0), chainMat);
      chain.position.set(
        Math.cos(angle) * 3.4,
        7.2,
        Math.sin(angle) * 3.4
      );
      chain.rotation.z = Math.cos(angle) * swingAngle;
      chain.rotation.x = Math.sin(angle) * swingAngle;
      group.add(chain);

      // seat
      const seatMat = new THREE.MeshPhysicalMaterial({
        color: seatColors[i],
        roughness: 0.5,
        clearcoat: 0.3,
      });
      const seat = new THREE.Mesh(new THREE.BoxGeometry(0.5, 0.12, 0.5), seatMat);
      seat.position.set(
        Math.cos(angle) * 3.8,
        5.5,
        Math.sin(angle) * 3.8
      );
      seat.castShadow = true;
      group.add(seat);
    }

    // lights on top
    const bulbMat = new THREE.MeshStandardMaterial({
      color: 0xffee88,
      emissive: 0xffdd44,
      emissiveIntensity: 0.5,
    });
    for (let i = 0; i < 12; i++) {
      const angle = (i / 12) * Math.PI * 2;
      const bulb = new THREE.Mesh(new THREE.SphereGeometry(0.06, 6, 6), bulbMat);
      bulb.position.set(Math.cos(angle) * 3.8, 8.3, Math.sin(angle) * 3.8);
      group.add(bulb);
    }
  }

  private createDefaultMarker(group: THREE.Group): void {
    // Carnival show pavilion / kiosk for generic "OTHER" attractions

    // --- Base platform ---
    const baseMat = new THREE.MeshStandardMaterial({ color: 0xdec9a0, roughness: 0.8 });
    const base = new THREE.Mesh(new THREE.CylinderGeometry(4, 4.3, 0.5, 8), baseMat);
    base.position.y = 0.25;
    base.castShadow = true;
    base.receiveShadow = true;
    group.add(base);

    // --- Tent poles (8 around perimeter) ---
    const poleMat = new THREE.MeshStandardMaterial({ color: 0x8b4513, roughness: 0.7 });
    for (let i = 0; i < 8; i++) {
      const angle = (i / 8) * Math.PI * 2;
      const pole = new THREE.Mesh(new THREE.CylinderGeometry(0.12, 0.12, 5, 6), poleMat);
      pole.position.set(Math.cos(angle) * 3.5, 3, Math.sin(angle) * 3.5);
      pole.castShadow = true;
      group.add(pole);
    }

    // --- Center pole ---
    const centerPole = new THREE.Mesh(new THREE.CylinderGeometry(0.18, 0.18, 7, 8), poleMat);
    centerPole.position.y = 3.5;
    centerPole.castShadow = true;
    group.add(centerPole);

    // --- Tent roof (cone) with stripes ---
    const roofCanvas = document.createElement('canvas');
    roofCanvas.width = 256;
    roofCanvas.height = 256;
    const rCtx = roofCanvas.getContext('2d')!;
    const stripeColors = ['#cc0000', '#ffffff'];
    const segments = 16;
    for (let i = 0; i < segments; i++) {
      rCtx.beginPath();
      rCtx.moveTo(128, 128);
      rCtx.arc(128, 128, 128, (i / segments) * Math.PI * 2, ((i + 1) / segments) * Math.PI * 2);
      rCtx.closePath();
      rCtx.fillStyle = stripeColors[i % 2];
      rCtx.fill();
    }
    const roofTexture = new THREE.CanvasTexture(roofCanvas);
    const roofMat = new THREE.MeshStandardMaterial({ map: roofTexture, roughness: 0.6, side: THREE.DoubleSide });
    const roof = new THREE.Mesh(new THREE.ConeGeometry(5, 3, 8, 1, true), roofMat);
    roof.position.y = 6.5;
    roof.castShadow = true;
    group.add(roof);

    // --- Roof brim (torus for overhang) ---
    const brimMat = new THREE.MeshStandardMaterial({ color: 0xcc0000, roughness: 0.6 });
    const brim = new THREE.Mesh(new THREE.TorusGeometry(4.2, 0.2, 8, 24), brimMat);
    brim.position.y = 5.5;
    brim.rotation.x = Math.PI / 2;
    group.add(brim);

    // --- Flag on top ---
    const flagPoleMat = new THREE.MeshStandardMaterial({ color: 0x444444 });
    const flagPole = new THREE.Mesh(new THREE.CylinderGeometry(0.05, 0.05, 1.5, 6), flagPoleMat);
    flagPole.position.y = 8.5;
    group.add(flagPole);

    const flagMat = new THREE.MeshStandardMaterial({ color: 0xffcc00, side: THREE.DoubleSide });
    const flag = new THREE.Mesh(new THREE.PlaneGeometry(0.9, 0.55), flagMat);
    flag.position.set(0.45, 9.0, 0);
    group.add(flag);

    // --- Scalloped valance / bunting around roof edge ---
    const buntingMat = new THREE.MeshStandardMaterial({
      color: 0xffdd44,
      roughness: 0.5,
      side: THREE.DoubleSide,
    });
    for (let i = 0; i < 16; i++) {
      const angle = (i / 16) * Math.PI * 2;
      const bunting = new THREE.Mesh(new THREE.SphereGeometry(0.25, 6, 4), buntingMat);
      bunting.position.set(Math.cos(angle) * 4.2, 5.3, Math.sin(angle) * 4.2);
      bunting.scale.y = 0.5;
      group.add(bunting);
    }

    // --- Decorative lights around poles ---
    const bulbMat = new THREE.MeshStandardMaterial({
      color: 0xffee88,
      emissive: 0xffdd44,
      emissiveIntensity: 0.6,
    });
    for (let i = 0; i < 8; i++) {
      const angle = (i / 8) * Math.PI * 2;
      const bulb = new THREE.Mesh(new THREE.SphereGeometry(0.1, 6, 6), bulbMat);
      bulb.position.set(Math.cos(angle) * 3.5, 5.3, Math.sin(angle) * 3.5);
      group.add(bulb);
    }

    // --- Star ornament on top ---
    const starMat = new THREE.MeshStandardMaterial({
      color: 0xffd700,
      emissive: 0xffaa00,
      emissiveIntensity: 0.4,
      metalness: 0.6,
    });
    const star = new THREE.Mesh(new THREE.OctahedronGeometry(0.35, 0), starMat);
    star.position.y = 9.4;
    star.rotation.y = Math.PI / 4;
    group.add(star);

    // --- Small stage / counter inside ---
    const counterMat = new THREE.MeshStandardMaterial({ color: 0x6b4226, roughness: 0.8 });
    const counter = new THREE.Mesh(new THREE.BoxGeometry(3, 1.2, 0.5), counterMat);
    counter.position.set(0, 1.1, 3.2);
    counter.castShadow = true;
    group.add(counter);

    // --- Counter top ---
    const topMat = new THREE.MeshStandardMaterial({ color: 0xf5deb3, roughness: 0.6 });
    const counterTop = new THREE.Mesh(new THREE.BoxGeometry(3.3, 0.1, 0.7), topMat);
    counterTop.position.set(0, 1.75, 3.2);
    group.add(counterTop);
  }
}
