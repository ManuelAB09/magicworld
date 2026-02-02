import * as THREE from 'three';
import { Attraction } from '../attraction/attraction.service';

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

    return group;
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
        const v = (Math.random() - 0.5) * 20;
        img.data[i] = Math.max(0, Math.min(255, img.data[i] + v));
        img.data[i + 1] = Math.max(0, Math.min(255, img.data[i + 1] + v));
        img.data[i + 2] = Math.max(0, Math.min(255, img.data[i + 2] + v));
      }
      ctx.putImageData(img, 0, 0);
    }
    const tex = new THREE.CanvasTexture(canvas);
    tex.wrapS = tex.wrapT = THREE.RepeatWrapping;
    tex.repeat.set(1, 1);
    // modern Three.js: use colorSpace
    (tex as any).colorSpace = THREE.SRGBColorSpace;
    return tex;
  }

  private createRollerCoaster(group: THREE.Group): void {
    const trackMat = new THREE.MeshPhysicalMaterial({
      map: this.makeCanvasTexture('#cc4444'),
      roughness: 0.6,
      metalness: 0.2,
      clearcoat: 0.2
    });
    const supportMat = new THREE.MeshStandardMaterial({ color: 0x666660, roughness: 0.8 });

    const curve = new THREE.CatmullRomCurve3([
      new THREE.Vector3(-6, 0.8, -3),
      new THREE.Vector3(-3, 3.6, -1),
      new THREE.Vector3(-1, 1.8, 2),
      new THREE.Vector3(2, 4.5, 1),
      new THREE.Vector3(5, 0.8, -2)
    ], true);

    const tubeGeometry = new THREE.TubeGeometry(curve, 200, 0.18, 12, true);
    const track = new THREE.Mesh(tubeGeometry, trackMat);
    track.castShadow = true;
    track.receiveShadow = true;
    group.add(track);

    const railGeo = new THREE.TubeGeometry(curve, 200, 0.06, 8, true);
    const leftRail = new THREE.Mesh(railGeo, supportMat);
    leftRail.position.x = -0.25;
    leftRail.castShadow = true;
    group.add(leftRail);
    const rightRail = leftRail.clone();
    rightRail.position.x = 0.25;
    group.add(rightRail);

    const supportCount = 18;
    for (let i = 0; i < supportCount; i++) {
      const t = i / supportCount;
      const p = curve.getPointAt(t);
      const h = Math.max(0.6, p.y);
      const left = new THREE.Mesh(new THREE.CylinderGeometry(0.06, 0.08, h, 6), supportMat);
      left.position.set(p.x - 0.4, h / 2, p.z);
      left.castShadow = true;
      group.add(left);

      const right = left.clone();
      right.position.set(p.x + 0.4, h / 2, p.z);
      group.add(right);

      const beam = new THREE.Mesh(new THREE.BoxGeometry(0.8, 0.06, 0.06), supportMat);
      beam.position.set(p.x, h * 0.6, p.z);
      beam.rotation.z = Math.random() * 0.02 - 0.01;
      group.add(beam);
    }

    const carMat = new THREE.MeshStandardMaterial({ color: 0x222222, roughness: 0.3, metalness: 0.6 });
    const car = new THREE.Mesh(new THREE.BoxGeometry(0.9, 0.35, 0.6), carMat);
    car.position.copy(curve.getPointAt(0));
    car.position.y += 0.35;
    car.castShadow = true;
    group.add(car);
  }

  private createFerrisWheel(group: THREE.Group): void {
    const wheelMat = new THREE.MeshPhysicalMaterial({
      map: this.makeCanvasTexture('#4477cc'),
      roughness: 0.5,
      metalness: 0.7,
      clearcoat: 0.1
    });
    const supportMat = new THREE.MeshStandardMaterial({ color: 0x555555 });

    const rim = new THREE.Mesh(new THREE.TorusGeometry(4.2, 0.18, 8, 64), wheelMat);
    rim.rotation.x = Math.PI / 2;
    rim.position.y = 6;
    rim.castShadow = true;
    group.add(rim);

    const spokes = 16;
    for (let i = 0; i < spokes; i++) {
      const angle = (i / spokes) * Math.PI * 2;
      const spoke = new THREE.Mesh(new THREE.BoxGeometry(0.08, 0.08, 4.2), supportMat);
      spoke.position.set(Math.cos(angle) * 2.1, 6, Math.sin(angle) * 2.1);
      spoke.rotation.y = -angle;
      spoke.castShadow = true;
      group.add(spoke);
    }

    const cabinMat = new THREE.MeshStandardMaterial({ color: 0xffcc66, roughness: 0.6 });
    const cabins = 12;
    for (let i = 0; i < cabins; i++) {
      const angle = (i / cabins) * Math.PI * 2;
      const cx = Math.cos(angle) * 4.0;
      const cz = Math.sin(angle) * 4.0;
      const cy = 6 + Math.sin(angle) * 0.12;
      const bar = new THREE.Mesh(new THREE.BoxGeometry(0.06, 0.06, 0.6), supportMat);
      bar.position.set(cx * 0.95, cy + 0.15, cz * 0.95);
      bar.rotation.y = angle;
      group.add(bar);

      const cabin = new THREE.Mesh(new THREE.BoxGeometry(0.9, 0.7, 0.7), cabinMat);
      cabin.position.set(cx, cy - 0.35, cz);
      cabin.lookAt(0, 6, 0);
      cabin.castShadow = true;
      group.add(cabin);
    }

    const supportLeft = new THREE.Mesh(new THREE.CylinderGeometry(0.25, 0.35, 6), supportMat);
    supportLeft.position.set(-1.6, 3, 0);
    supportLeft.rotation.z = 0.12;
    supportLeft.castShadow = true;
    group.add(supportLeft);

    const supportRight = supportLeft.clone();
    supportRight.position.set(1.6, 3, 0);
    supportRight.rotation.z = -0.12;
    group.add(supportRight);
  }

  private createCarousel(group: THREE.Group): void {
    const baseMat = new THREE.MeshPhysicalMaterial({ color: 0xffc0cb, roughness: 0.7, metalness: 0.1 });
    const roofMat = new THREE.MeshPhysicalMaterial({ color: 0xffdd55, roughness: 0.5, metalness: 0.05 });
    const poleMat = new THREE.MeshStandardMaterial({ color: 0xdddddd });

    const base = new THREE.Mesh(new THREE.CylinderGeometry(3.0, 3.0, 0.5, 32), baseMat);
    base.position.y = 0.25;
    base.receiveShadow = true;
    group.add(base);

    const roof = new THREE.Mesh(new THREE.ConeGeometry(3.6, 1.8, 32), roofMat);
    roof.position.y = 3.6;
    roof.castShadow = true;
    group.add(roof);

    const centerPole = new THREE.Mesh(new THREE.CylinderGeometry(0.18, 0.18, 3.2), poleMat);
    centerPole.position.y = 1.6;
    group.add(centerPole);

    for (let i = 0; i < 12; i++) {
      const stripe = new THREE.Mesh(new THREE.PlaneGeometry(0.5, 1.4), new THREE.MeshStandardMaterial({ color: i % 2 === 0 ? 0xffffff : 0xff9999 }));
      const a = (i / 12) * Math.PI * 2;
      stripe.position.set(Math.cos(a) * 2.6, 3.3, Math.sin(a) * 2.6);
      stripe.lookAt(0, 3.3, 0);
      stripe.receiveShadow = true;
      group.add(stripe);
    }

    for (let i = 0; i < 8; i++) {
      const angle = (i / 8) * Math.PI * 2;
      const horse = new THREE.Mesh(new THREE.BoxGeometry(0.6, 0.4, 0.25), new THREE.MeshStandardMaterial({ color: 0xffffff }));
      horse.position.set(Math.cos(angle) * 2.0, 0.9, Math.sin(angle) * 2.0);
      horse.castShadow = true;
      group.add(horse);

      const rod = new THREE.Mesh(new THREE.CylinderGeometry(0.02, 0.02, 1.4), poleMat);
      rod.position.set(Math.cos(angle) * 2.0, 1.6, Math.sin(angle) * 2.0);
      group.add(rod);
    }
  }

  private createDropTower(group: THREE.Group): void {
    const towerMat = new THREE.MeshStandardMaterial({ color: 0x444444, roughness: 0.9 });
    const seatMat = new THREE.MeshPhysicalMaterial({ color: 0xff4444, roughness: 0.4, metalness: 0.1 });

    const tower = new THREE.Mesh(new THREE.BoxGeometry(1.5, 12, 1.5), towerMat);
    tower.position.y = 6;
    tower.castShadow = true;
    group.add(tower);

    const seat = new THREE.Mesh(new THREE.BoxGeometry(1.6, 0.4, 1.2), seatMat);
    seat.position.y = 8.0;
    seat.castShadow = true;
    group.add(seat);

    const top = new THREE.Mesh(new THREE.ConeGeometry(1.2, 1.5, 6), towerMat);
    top.position.y = 12.75;
    group.add(top);
  }

  private createHauntedHouse(group: THREE.Group): void {
    const wallMat = new THREE.MeshStandardMaterial({ color: 0x48372f, roughness: 0.9 });
    const roofMat = new THREE.MeshStandardMaterial({ color: 0x2b1a13, roughness: 1 });

    const house = new THREE.Mesh(new THREE.BoxGeometry(6, 4.5, 4.2), wallMat);
    house.position.y = 2.25;
    house.castShadow = true;
    group.add(house);

    const roof = new THREE.Mesh(new THREE.ConeGeometry(4.5, 2, 4), roofMat);
    roof.position.y = 5;
    roof.rotation.y = Math.PI / 4;
    roof.castShadow = true;
    group.add(roof);

    const windowMat = new THREE.MeshStandardMaterial({ color: 0x111111, emissive: 0x220000, emissiveIntensity: 0.01 });
    for (let i = -1; i <= 1; i++) {
      const w = new THREE.Mesh(new THREE.BoxGeometry(0.6, 0.8, 0.05), windowMat);
      w.position.set(i * 1.5, 2.6, 2.11);
      group.add(w);
    }
  }

  private createWaterRide(group: THREE.Group): void {
    const stoneMat = new THREE.MeshStandardMaterial({ color: 0x777777, roughness: 0.9 });
    const waterMat = new THREE.MeshPhysicalMaterial({
      color: 0x2b7bd9,
      metalness: 0.1,
      roughness: 0.05,
      reflectivity: 0.7,
      clearcoat: 0.2,
      transparent: true,
      opacity: 0.9
    });

    const pool = new THREE.Mesh(new THREE.CylinderGeometry(3, 3, 0.6, 32), stoneMat);
    pool.position.y = 0.3;
    pool.receiveShadow = true;
    group.add(pool);

    const waterGeo = new THREE.CircleGeometry(2.6, 64);
    const water = new THREE.Mesh(waterGeo, waterMat);
    water.rotation.x = -Math.PI / 2;
    water.position.y = 0.55;
    // safe attribute access
    (water.geometry as THREE.BufferGeometry).setAttribute(
      'uNoiseOffset',
      new THREE.BufferAttribute(new Float32Array((waterGeo as any).attributes['position'].count), 1)
    );
    water.castShadow = false;
    group.add(water);

    const slideCurve = new THREE.CatmullRomCurve3([
      new THREE.Vector3(-1.5, 4.8, -2.5),
      new THREE.Vector3(-0.4, 3, -1.2),
      new THREE.Vector3(0.2, 2, 0),
      new THREE.Vector3(1.4, 1.0, 1.3)
    ]);
    const slideGeom = new THREE.TubeGeometry(slideCurve, 60, 0.25, 8, false);
    const slide = new THREE.Mesh(slideGeom, new THREE.MeshStandardMaterial({ color: 0xffffff, roughness: 0.6 }));
    slide.castShadow = true;
    group.add(slide);

    const platform = new THREE.Mesh(new THREE.BoxGeometry(2.2, 0.3, 1.6), stoneMat);
    platform.position.set(-1.5, 4.8, -2.5);
    group.add(platform);
  }

  private createBumperCars(group: THREE.Group): void {
    const floorMat = new THREE.MeshStandardMaterial({ color: 0x333333 });
    const carColors = [0xff4d4d, 0x4dff88, 0x4da6ff, 0xffe34d, 0xff79ff];

    const floor = new THREE.Mesh(new THREE.CylinderGeometry(4.2, 4.2, 0.2, 32), floorMat);
    floor.position.y = 0.1;
    floor.receiveShadow = true;
    group.add(floor);

    for (let i = 0; i < 5; i++) {
      const angle = (i / 5) * Math.PI * 2;
      const car = new THREE.Mesh(
        new THREE.SphereGeometry(0.62, 12, 12),
        new THREE.MeshPhysicalMaterial({ color: carColors[i], roughness: 0.3, metalness: 0.2 })
      );
      car.position.set(Math.cos(angle) * 2.5, 0.45, Math.sin(angle) * 2.5);
      car.scale.set(1, 0.6, 1);
      car.castShadow = true;
      group.add(car);
    }

    const fence = new THREE.Mesh(new THREE.TorusGeometry(4.4, 0.08, 8, 64), new THREE.MeshStandardMaterial({ color: 0x888888 }));
    fence.position.y = 0.9;
    fence.rotation.x = Math.PI / 2;
    group.add(fence);
  }

  private createTrainRide(group: THREE.Group): void {
    const trackMaterial = new THREE.LineBasicMaterial({ color: 0x6b3f1f });
    const trainMaterial = new THREE.MeshPhysicalMaterial({ color: 0x2b8b2b, roughness: 0.5 });

    const trackCurve = new THREE.EllipseCurve(0, 0, 4.2, 3.1);
    const trackPoints = trackCurve.getPoints(200);
    const pts = trackPoints.map(p => new THREE.Vector3(p.x, 0.1, p.y));
    const trackGeometry = new THREE.BufferGeometry().setFromPoints(pts);
    const track = new THREE.Line(trackGeometry, trackMaterial);
    group.add(track);

    const engine = new THREE.Mesh(new THREE.BoxGeometry(1.2, 1, 0.9), trainMaterial);
    engine.position.set(4.2, 0.55, 0);
    engine.castShadow = true;
    group.add(engine);

    const chimney = new THREE.Mesh(new THREE.CylinderGeometry(0.12, 0.15, 0.6), new THREE.MeshStandardMaterial({ color: 0x111111 }));
    chimney.position.set(4.7, 1.05, 0);
    group.add(chimney);

    for (let i = 1; i <= 2; i++) {
      const car = new THREE.Mesh(new THREE.BoxGeometry(0.84, 0.6, 0.72), new THREE.MeshStandardMaterial({ color: 0xcc4444 }));
      car.position.set(4.2 - i * 1.25, 0.45, 0);
      car.castShadow = true;
      group.add(car);
    }
  }

  private createSwingRide(group: THREE.Group): void {
    const poleMat = new THREE.MeshStandardMaterial({ color: 0xffd700 });
    const topMat = new THREE.MeshStandardMaterial({ color: 0xff6b6b });

    const centerPole = new THREE.Mesh(new THREE.CylinderGeometry(0.28, 0.36, 8.4), poleMat);
    centerPole.position.y = 4.2;
    centerPole.castShadow = true;
    group.add(centerPole);

    const top = new THREE.Mesh(new THREE.CylinderGeometry(3.6, 3.6, 0.9, 24), topMat);
    top.position.y = 8.2;
    top.castShadow = true;
    group.add(top);

    for (let i = 0; i < 10; i++) {
      const angle = (i / 10) * Math.PI * 2;
      const chain = new THREE.Mesh(new THREE.CylinderGeometry(0.02, 0.02, 2.8), new THREE.MeshStandardMaterial({ color: 0xaaaaaa }));
      chain.position.set(Math.cos(angle) * 3.2, 6.8, Math.sin(angle) * 3.2);
      chain.rotation.z = Math.cos(angle) * 0.25;
      group.add(chain);

      const seat = new THREE.Mesh(new THREE.BoxGeometry(0.46, 0.11, 0.46), new THREE.MeshStandardMaterial({ color: 0x4466ff }));
      seat.position.set(Math.cos(angle) * 3.6, 5.1, Math.sin(angle) * 3.6);
      seat.castShadow = true;
      group.add(seat);
    }
  }

  private createDefaultMarker(group: THREE.Group): void {
    const pinMat = new THREE.MeshStandardMaterial({ color: 0xff4444, roughness: 0.6 });
    const baseMat = new THREE.MeshStandardMaterial({ color: 0xffffff, roughness: 0.9 });

    const pin = new THREE.Mesh(new THREE.ConeGeometry(0.7, 2.1, 12), pinMat);
    pin.position.y = 1.05;
    pin.castShadow = true;
    group.add(pin);

    const head = new THREE.Mesh(new THREE.SphereGeometry(0.9, 12, 12), pinMat);
    head.position.y = 2.6;
    head.castShadow = true;
    group.add(head);

    const base = new THREE.Mesh(new THREE.CylinderGeometry(1.1, 1.1, 0.25, 12), baseMat);
    base.position.y = 0.125;
    group.add(base);
  }
}
