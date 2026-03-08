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
      roughness: 0.35,
      metalness: 0.4,
      clearcoat: 0.5,
      clearcoatRoughness: 0.1,
    });
    const supportMat = new THREE.MeshStandardMaterial({ color: 0x555555, roughness: 0.6, metalness: 0.5 });

    // main track with loop – more control points for smoothness
    const curve = new THREE.CatmullRomCurve3([
      new THREE.Vector3(-6, 0.8, -3),
      new THREE.Vector3(-5, 3.0, -2.5),
      new THREE.Vector3(-4, 5.0, -1.5),
      new THREE.Vector3(-2.5, 3.5, 0),
      new THREE.Vector3(-1, 2.0, 1.5),
      new THREE.Vector3(0, 6.5, 2.5),
      new THREE.Vector3(1.5, 5.0, 1.5),
      new THREE.Vector3(2.5, 4.0, 0),
      new THREE.Vector3(3.5, 2.5, -1.5),
      new THREE.Vector3(5, 0.8, -2),
    ], true);

    const tubeGeometry = new THREE.TubeGeometry(curve, 300, 0.18, 16, true);
    const track = new THREE.Mesh(tubeGeometry, trackMat);
    track.castShadow = true;
    track.receiveShadow = true;
    group.add(track);

    // double rails
    const railGeo = new THREE.TubeGeometry(curve, 300, 0.06, 10, true);
    const railMat = new THREE.MeshPhysicalMaterial({ color: 0x999999, metalness: 0.7, roughness: 0.3, clearcoat: 0.2 });
    const leftRail = new THREE.Mesh(railGeo, railMat);
    leftRail.position.x = -0.28;
    leftRail.castShadow = true;
    group.add(leftRail);
    const rightRail = leftRail.clone();
    rightRail.position.x = 0.28;
    group.add(rightRail);

    // supports with cross-bracing
    const supportCount = 28;
    for (let i = 0; i < supportCount; i++) {
      const t = i / supportCount;
      const p = curve.getPointAt(t);
      const h = Math.max(0.6, p.y);

      const left = new THREE.Mesh(new THREE.CylinderGeometry(0.05, 0.07, h, 8), supportMat);
      left.position.set(p.x - 0.45, h / 2, p.z);
      left.castShadow = true;
      group.add(left);

      const right = left.clone();
      right.position.set(p.x + 0.45, h / 2, p.z);
      group.add(right);

      // cross brace
      if (h > 1.2) {
        const brace = new THREE.Mesh(new THREE.BoxGeometry(0.9, 0.04, 0.04), supportMat);
        brace.position.set(p.x, h * 0.5, p.z);
        brace.rotation.z = 0.3;
        group.add(brace);
      }
      // additional horizontal brace
      if (h > 2.5) {
        const hBrace = new THREE.Mesh(new THREE.BoxGeometry(0.9, 0.04, 0.04), supportMat);
        hBrace.position.set(p.x, h * 0.75, p.z);
        group.add(hBrace);
      }
    }

    // train cars – more detailed
    const carColors = [0x222222, 0xcc0000, 0x0055cc, 0xffcc00];
    for (let c = 0; c < 4; c++) {
      const carMat = new THREE.MeshPhysicalMaterial({
        color: carColors[c],
        roughness: 0.2,
        metalness: 0.6,
        clearcoat: 0.6,
        clearcoatRoughness: 0.05,
      });
      const car = new THREE.Mesh(new THREE.BoxGeometry(0.8, 0.35, 0.55), carMat);
      const pt = curve.getPointAt(c * 0.035);
      car.position.set(pt.x, pt.y + 0.30, pt.z);
      car.castShadow = true;
      group.add(car);

      // headrest
      const headrest = new THREE.Mesh(
        new THREE.BoxGeometry(0.6, 0.2, 0.08),
        new THREE.MeshStandardMaterial({ color: 0x333333, roughness: 0.8 })
      );
      headrest.position.set(pt.x, pt.y + 0.55, pt.z - 0.2);
      group.add(headrest);

      // wheels
      const wheelMat = new THREE.MeshStandardMaterial({ color: 0x222222, metalness: 0.5 });
      for (const dz of [-0.3, 0.3]) {
        const wheel = new THREE.Mesh(new THREE.CylinderGeometry(0.08, 0.08, 0.04, 10), wheelMat);
        wheel.position.set(pt.x, pt.y + 0.10, pt.z + dz);
        wheel.rotation.x = Math.PI / 2;
        group.add(wheel);
      }
    }

    // station – more detailed
    const stationMat = new THREE.MeshStandardMaterial({ color: 0x8b4513, roughness: 0.8 });
    const station = new THREE.Mesh(new THREE.BoxGeometry(3, 2, 2.2), stationMat);
    station.position.set(-6, 1, -3);
    station.castShadow = true;
    group.add(station);

    // station windows
    const winMat = new THREE.MeshStandardMaterial({ color: 0x88ccff, emissive: 0x224466, emissiveIntensity: 0.15 });
    for (const dx of [-0.8, 0.8]) {
      const win = new THREE.Mesh(new THREE.BoxGeometry(0.5, 0.6, 0.05), winMat);
      win.position.set(-6 + dx, 1.2, -1.88);
      group.add(win);
    }

    const stationRoof = new THREE.Mesh(
      new THREE.ConeGeometry(2.2, 1.2, 4),
      new THREE.MeshStandardMaterial({ color: 0xcc0000, roughness: 0.6 })
    );
    stationRoof.position.set(-6, 2.6, -3);
    stationRoof.rotation.y = Math.PI / 4;
    group.add(stationRoof);

    // sign
    const signMat = new THREE.MeshStandardMaterial({ color: 0xffdd00, emissive: 0xffaa00, emissiveIntensity: 0.3 });
    const sign = new THREE.Mesh(new THREE.BoxGeometry(2.5, 0.4, 0.1), signMat);
    sign.position.set(-6, 2.1, -1.85);
    group.add(sign);
  }

  private createFerrisWheel(group: THREE.Group): void {
    const wheelMat = new THREE.MeshPhysicalMaterial({
      map: this.makeCanvasTexture('#3366aa'),
      roughness: 0.3,
      metalness: 0.75,
      clearcoat: 0.3,
      clearcoatRoughness: 0.1,
    });
    const supportMat = new THREE.MeshPhysicalMaterial({ color: 0x444444, metalness: 0.6, roughness: 0.4, clearcoat: 0.15 });

    // outer rim
    const rim = new THREE.Mesh(new THREE.TorusGeometry(4.5, 0.18, 16, 80), wheelMat);
    rim.rotation.x = Math.PI / 2;
    rim.position.y = 6.5;
    rim.castShadow = true;
    group.add(rim);

    // inner rim
    const innerRim = new THREE.Mesh(new THREE.TorusGeometry(3.5, 0.07, 12, 64), supportMat);
    innerRim.rotation.x = Math.PI / 2;
    innerRim.position.y = 6.5;
    group.add(innerRim);

    // middle rim
    const midRim = new THREE.Mesh(new THREE.TorusGeometry(2.2, 0.05, 10, 48), supportMat);
    midRim.rotation.x = Math.PI / 2;
    midRim.position.y = 6.5;
    group.add(midRim);

    // spokes – more of them for detail
    const spokes = 20;
    for (let i = 0; i < spokes; i++) {
      const angle = (i / spokes) * Math.PI * 2;
      const spoke = new THREE.Mesh(new THREE.CylinderGeometry(0.04, 0.04, 4.5, 8), supportMat);
      spoke.position.set(0, 6.5, 0);
      spoke.rotation.x = Math.PI / 2;
      spoke.rotation.z = angle;
      spoke.translateY(2.25);
      // Reset and use proper positioning
      spoke.position.set(Math.cos(angle) * 2.25, 6.5, Math.sin(angle) * 2.25);
      spoke.lookAt(0, 6.5, 0);
      spoke.castShadow = true;
      group.add(spoke);
    }

    // cabins with varied colours and more detail
    const cabinColors = [0xff6644, 0x44aa66, 0x4488cc, 0xeecc44, 0xcc44cc, 0x44cccc, 0xff8866, 0x66bb88, 0x6699dd, 0xddaa33, 0xaa55bb, 0x55bbaa];
    const cabins = 12;
    for (let i = 0; i < cabins; i++) {
      const angle = (i / cabins) * Math.PI * 2;
      const cx = Math.cos(angle) * 4.3;
      const cz = Math.sin(angle) * 4.3;
      const cy = 6.5;

      // hanging bar
      const bar = new THREE.Mesh(new THREE.CylinderGeometry(0.025, 0.025, 0.9, 8), supportMat);
      bar.position.set(cx * 0.96, cy - 0.15, cz * 0.96);
      group.add(bar);

      // cabin body – rounded
      const cabinMat = new THREE.MeshPhysicalMaterial({
        color: cabinColors[i % cabinColors.length],
        roughness: 0.4,
        metalness: 0.1,
        clearcoat: 0.4,
        clearcoatRoughness: 0.1,
      });
      const cabin = new THREE.Mesh(new THREE.BoxGeometry(0.85, 0.75, 0.75, 2, 2, 2), cabinMat);
      cabin.position.set(cx, cy - 0.65, cz);
      cabin.lookAt(0, 6.5, 0);
      cabin.castShadow = true;
      group.add(cabin);

      // cabin window (glass)
      const glassMat = new THREE.MeshPhysicalMaterial({
        color: 0x88ccff,
        roughness: 0.05,
        metalness: 0.0,
        transparent: true,
        opacity: 0.5,
        clearcoat: 1.0,
      });
      const windowMesh = new THREE.Mesh(new THREE.PlaneGeometry(0.55, 0.4), glassMat);
      windowMesh.position.set(cx * 1.02, cy - 0.6, cz * 1.02);
      windowMesh.lookAt(cx * 2, cy - 0.6, cz * 2);
      group.add(windowMesh);

      // cabin roof
      const roofMat = new THREE.MeshStandardMaterial({ color: 0x333333, roughness: 0.7, metalness: 0.3 });
      const cabinRoof = new THREE.Mesh(new THREE.BoxGeometry(0.95, 0.08, 0.85), roofMat);
      cabinRoof.position.set(cx, cy - 0.2, cz);
      cabinRoof.lookAt(0, 6.5, 0);
      group.add(cabinRoof);
    }

    // A-frame supports – thicker and more detailed
    for (const side of [-1.8, 1.8]) {
      const aLeg = new THREE.Mesh(new THREE.CylinderGeometry(0.25, 0.4, 7.2, 12), supportMat);
      aLeg.position.set(side, 3.5, 0);
      aLeg.rotation.z = side > 0 ? -0.14 : 0.14;
      aLeg.castShadow = true;
      group.add(aLeg);

      // Cross brace on A-frame
      const crossBrace = new THREE.Mesh(new THREE.BoxGeometry(0.08, 0.08, 2.0), supportMat);
      crossBrace.position.set(side * 0.7, 2.0, 0);
      crossBrace.rotation.z = side > 0 ? 0.5 : -0.5;
      group.add(crossBrace);
    }

    // base cross beam
    const crossBeam = new THREE.Mesh(new THREE.BoxGeometry(4.5, 0.3, 0.3), supportMat);
    crossBeam.position.set(0, 0.15, 0);
    crossBeam.castShadow = true;
    group.add(crossBeam);

    // hub – more detailed
    const hubOuter = new THREE.Mesh(new THREE.TorusGeometry(0.35, 0.08, 12, 24), wheelMat);
    hubOuter.rotation.x = Math.PI / 2;
    hubOuter.position.y = 6.5;
    group.add(hubOuter);
    const hubCenter = new THREE.Mesh(new THREE.SphereGeometry(0.3, 16, 16), wheelMat);
    hubCenter.position.y = 6.5;
    group.add(hubCenter);

    // lights on rim
    const lightMat = new THREE.MeshStandardMaterial({
      color: 0xffee88,
      emissive: 0xffdd44,
      emissiveIntensity: 0.7,
    });
    for (let i = 0; i < 32; i++) {
      const angle = (i / 32) * Math.PI * 2;
      const bulb = new THREE.Mesh(new THREE.SphereGeometry(0.07, 8, 8), lightMat);
      bulb.position.set(Math.cos(angle) * 4.5, 6.5, Math.sin(angle) * 4.5);
      group.add(bulb);
    }

    // lights on inner rim
    for (let i = 0; i < 16; i++) {
      const angle = (i / 16) * Math.PI * 2;
      const bulb = new THREE.Mesh(new THREE.SphereGeometry(0.05, 6, 6), lightMat);
      bulb.position.set(Math.cos(angle) * 3.5, 6.5, Math.sin(angle) * 3.5);
      group.add(bulb);
    }
  }

  private createCarousel(group: THREE.Group): void {
    const baseMat = new THREE.MeshPhysicalMaterial({ color: 0xffc0cb, roughness: 0.5, metalness: 0.1, clearcoat: 0.3, clearcoatRoughness: 0.1 });
    const roofMat = new THREE.MeshPhysicalMaterial({ color: 0xffdd55, roughness: 0.35, metalness: 0.15, clearcoat: 0.4 });
    const poleMat = new THREE.MeshPhysicalMaterial({ color: 0xdddddd, metalness: 0.6, roughness: 0.2, clearcoat: 0.3 });

    // ornate base
    const base = new THREE.Mesh(new THREE.CylinderGeometry(3.2, 3.4, 0.6, 48), baseMat);
    base.position.y = 0.3;
    base.receiveShadow = true;
    group.add(base);

    // base trim rings (double)
    const goldMat = new THREE.MeshPhysicalMaterial({ color: 0xffd700, metalness: 0.7, roughness: 0.2, clearcoat: 0.5 });
    const trimRing = new THREE.Mesh(new THREE.TorusGeometry(3.3, 0.07, 12, 48), goldMat);
    trimRing.position.y = 0.6;
    trimRing.rotation.x = Math.PI / 2;
    group.add(trimRing);

    const trimRingBottom = new THREE.Mesh(new THREE.TorusGeometry(3.35, 0.05, 10, 48), goldMat);
    trimRingBottom.position.y = 0.05;
    trimRingBottom.rotation.x = Math.PI / 2;
    group.add(trimRingBottom);

    // conical roof – more segments
    const roof = new THREE.Mesh(new THREE.ConeGeometry(3.8, 2.0, 48), roofMat);
    roof.position.y = 3.8;
    roof.castShadow = true;
    group.add(roof);

    // roof trim ring
    const roofTrim = new THREE.Mesh(new THREE.TorusGeometry(3.8, 0.06, 10, 48), goldMat);
    roofTrim.position.y = 2.8;
    roofTrim.rotation.x = Math.PI / 2;
    group.add(roofTrim);

    // roof finial – ornamental
    const finial = new THREE.Mesh(new THREE.SphereGeometry(0.22, 16, 16), goldMat);
    finial.position.y = 4.9;
    group.add(finial);
    const finialSpike = new THREE.Mesh(new THREE.ConeGeometry(0.08, 0.4, 8), goldMat);
    finialSpike.position.y = 5.2;
    group.add(finialSpike);

    // center pole – with rings
    const centerPole = new THREE.Mesh(new THREE.CylinderGeometry(0.18, 0.18, 3.5, 12), poleMat);
    centerPole.position.y = 1.75;
    group.add(centerPole);

    // decorative rings on center pole
    for (const h of [1.0, 2.0, 3.0]) {
      const ring = new THREE.Mesh(new THREE.TorusGeometry(0.22, 0.03, 8, 16), goldMat);
      ring.position.y = h;
      ring.rotation.x = Math.PI / 2;
      group.add(ring);
    }

    // hanging banners / stripes
    for (let i = 0; i < 16; i++) {
      const color = i % 2 === 0 ? 0xffffff : 0xff8888;
      const stripe = new THREE.Mesh(
        new THREE.PlaneGeometry(0.5, 1.6),
        new THREE.MeshStandardMaterial({ color, side: THREE.DoubleSide, roughness: 0.6 })
      );
      const a = (i / 16) * Math.PI * 2;
      stripe.position.set(Math.cos(a) * 2.8, 3.4, Math.sin(a) * 2.8);
      stripe.lookAt(0, 3.4, 0);
      stripe.receiveShadow = true;
      group.add(stripe);
    }

    // horses – improved shapes
    const horseColors = [0xffffff, 0xeecc88, 0xaa8866, 0xcccccc, 0xffddaa, 0xddbbaa, 0xe8d8c8, 0xf0e0d0];
    for (let i = 0; i < 8; i++) {
      const angle = (i / 8) * Math.PI * 2;
      const horseMat = new THREE.MeshPhysicalMaterial({
        color: horseColors[i],
        roughness: 0.5,
        metalness: 0.05,
        clearcoat: 0.3,
      });

      // horse body – slightly rounder
      const body = new THREE.Mesh(new THREE.CapsuleGeometry(0.15, 0.35, 8, 12), horseMat);
      body.position.set(Math.cos(angle) * 2.2, 1.0, Math.sin(angle) * 2.2);
      body.rotation.z = Math.PI / 2;
      body.rotation.y = angle;
      body.castShadow = true;
      group.add(body);

      // horse head
      const head = new THREE.Mesh(new THREE.SphereGeometry(0.12, 10, 10), horseMat);
      head.position.set(
        Math.cos(angle) * 2.2 + Math.cos(angle) * 0.35,
        1.18,
        Math.sin(angle) * 2.2 + Math.sin(angle) * 0.35
      );
      group.add(head);

      // horse legs (4)
      for (const legOff of [-0.12, 0.12]) {
        for (const fwd of [-0.1, 0.1]) {
          const leg = new THREE.Mesh(new THREE.CylinderGeometry(0.025, 0.02, 0.3, 6), horseMat);
          leg.position.set(
            Math.cos(angle) * 2.2 + Math.cos(angle) * fwd - Math.sin(angle) * legOff,
            0.7,
            Math.sin(angle) * 2.2 + Math.sin(angle) * fwd + Math.cos(angle) * legOff
          );
          group.add(leg);
        }
      }

      // saddle
      const saddleMat = new THREE.MeshPhysicalMaterial({ color: 0xcc2222, roughness: 0.4, clearcoat: 0.3 });
      const saddle = new THREE.Mesh(new THREE.BoxGeometry(0.18, 0.06, 0.22), saddleMat);
      saddle.position.set(Math.cos(angle) * 2.2, 1.18, Math.sin(angle) * 2.2);
      group.add(saddle);

      // pole through horse
      const rod = new THREE.Mesh(new THREE.CylinderGeometry(0.02, 0.02, 1.8, 8), poleMat);
      rod.position.set(Math.cos(angle) * 2.2, 1.7, Math.sin(angle) * 2.2);
      group.add(rod);
    }

    // light bulbs around rim
    const bulbMat = new THREE.MeshStandardMaterial({
      color: 0xffeeaa,
      emissive: 0xffcc44,
      emissiveIntensity: 0.6,
    });
    for (let i = 0; i < 24; i++) {
      const angle = (i / 24) * Math.PI * 2;
      const bulb = new THREE.Mesh(new THREE.SphereGeometry(0.06, 8, 8), bulbMat);
      bulb.position.set(Math.cos(angle) * 3.5, 2.85, Math.sin(angle) * 3.5);
      group.add(bulb);
    }

    // mirror panels on base
    const mirrorMat = new THREE.MeshPhysicalMaterial({
      color: 0xffffff,
      metalness: 0.9,
      roughness: 0.05,
      clearcoat: 1.0,
    });
    for (let i = 0; i < 12; i++) {
      const angle = (i / 12) * Math.PI * 2;
      const mirror = new THREE.Mesh(new THREE.PlaneGeometry(0.5, 0.35), mirrorMat);
      mirror.position.set(Math.cos(angle) * 3.25, 0.35, Math.sin(angle) * 3.25);
      mirror.lookAt(0, 0.35, 0);
      group.add(mirror);
    }
  }

  private createDropTower(group: THREE.Group): void {
    const towerMat = new THREE.MeshPhysicalMaterial({ color: 0x383838, roughness: 0.7, metalness: 0.4, clearcoat: 0.1 });
    const seatMat = new THREE.MeshPhysicalMaterial({ color: 0xff3333, roughness: 0.3, metalness: 0.2, clearcoat: 0.4, clearcoatRoughness: 0.1 });
    const frameMat = new THREE.MeshPhysicalMaterial({ color: 0x555555, metalness: 0.6, roughness: 0.4, clearcoat: 0.15 });

    // main tower structure – four corner pillars instead of solid box
    for (const dx of [-0.5, 0.5]) {
      for (const dz of [-0.5, 0.5]) {
        const pillar = new THREE.Mesh(new THREE.CylinderGeometry(0.15, 0.18, 13, 10), towerMat);
        pillar.position.set(dx, 6.5, dz);
        pillar.castShadow = true;
        group.add(pillar);
      }
    }

    // lattice cross pieces – more detailed
    for (let h = 0.5; h < 13; h += 1.0) {
      // horizontal braces on all 4 sides
      for (let side = 0; side < 4; side++) {
        const isX = side < 2;
        const sign = side % 2 === 0 ? 1 : -1;
        const cross = new THREE.Mesh(new THREE.BoxGeometry(isX ? 1.0 : 0.04, 0.04, isX ? 0.04 : 1.0), frameMat);
        cross.position.set(isX ? 0 : sign * 0.5, h, isX ? sign * 0.5 : 0);
        group.add(cross);
      }

      // diagonal brace (X pattern)
      if (h % 2 === 0) {
        const diag = new THREE.Mesh(new THREE.BoxGeometry(1.3, 0.03, 0.03), frameMat);
        diag.position.set(0, h + 0.5, 0.5);
        diag.rotation.z = 0.75;
        group.add(diag);
        const diag2 = diag.clone();
        diag2.rotation.z = -0.75;
        group.add(diag2);
      }
    }

    // vertical guide rails
    const railMat = new THREE.MeshPhysicalMaterial({ color: 0x666666, metalness: 0.7, roughness: 0.3 });
    for (const dx of [-0.3, 0.3]) {
      const rail = new THREE.Mesh(new THREE.BoxGeometry(0.08, 13, 0.08), railMat);
      rail.position.set(dx, 6.5, 0.5);
      group.add(rail);
    }

    // seat ring – toroidal
    const seatRing = new THREE.Mesh(new THREE.TorusGeometry(2.0, 0.22, 12, 24), seatMat);
    seatRing.position.y = 9;
    seatRing.rotation.x = Math.PI / 2;
    seatRing.castShadow = true;
    group.add(seatRing);

    // individual seats with restraints
    for (let i = 0; i < 10; i++) {
      const angle = (i / 10) * Math.PI * 2;
      const seat = new THREE.Mesh(new THREE.BoxGeometry(0.4, 0.5, 0.3), seatMat);
      seat.position.set(Math.cos(angle) * 2.0, 8.7, Math.sin(angle) * 2.0);
      seat.lookAt(0, 8.7, 0);
      seat.castShadow = true;
      group.add(seat);

      // shoulder restraint bar
      const restraint = new THREE.Mesh(
        new THREE.TorusGeometry(0.15, 0.02, 6, 8, Math.PI),
        new THREE.MeshStandardMaterial({ color: 0x333333, metalness: 0.5 })
      );
      restraint.position.set(Math.cos(angle) * 2.0, 9.1, Math.sin(angle) * 2.0);
      restraint.lookAt(0, 9.1, 0);
      group.add(restraint);
    }

    // top cap – more ornate
    const top = new THREE.Mesh(new THREE.ConeGeometry(1.2, 2, 8), towerMat);
    top.position.y = 14;
    group.add(top);

    // top platform ring
    const topRing = new THREE.Mesh(new THREE.TorusGeometry(1.2, 0.08, 8, 16), frameMat);
    topRing.position.y = 13;
    topRing.rotation.x = Math.PI / 2;
    group.add(topRing);

    // strobe lights at top (dual)
    const strobeMat = new THREE.MeshStandardMaterial({
      color: 0xff0000,
      emissive: 0xff0000,
      emissiveIntensity: 1.2,
    });
    const strobe = new THREE.Mesh(new THREE.SphereGeometry(0.25, 10, 10), strobeMat);
    strobe.position.y = 15;
    group.add(strobe);

    const strobe2 = new THREE.Mesh(new THREE.SphereGeometry(0.2, 10, 10), strobeMat);
    strobe2.position.set(0, 14.5, 0.8);
    group.add(strobe2);

    // base – wider foot with braces
    const baseMat = new THREE.MeshStandardMaterial({ color: 0x444444, roughness: 0.8, metalness: 0.3 });
    for (const angle of [0, Math.PI / 2, Math.PI, Math.PI * 1.5]) {
      const foot = new THREE.Mesh(new THREE.BoxGeometry(0.3, 0.3, 2.5), baseMat);
      foot.position.set(Math.cos(angle) * 1.2, 0.15, Math.sin(angle) * 1.2);
      foot.rotation.y = angle;
      group.add(foot);
    }

    // name sign
    const signMat = new THREE.MeshStandardMaterial({ color: 0xffdd00, emissive: 0xffaa00, emissiveIntensity: 0.25 });
    const sign = new THREE.Mesh(new THREE.BoxGeometry(2.0, 0.5, 0.08), signMat);
    sign.position.set(0, 1.5, 1.5);
    group.add(sign);
  }

  private createHauntedHouse(group: THREE.Group): void {
    const wallMat = new THREE.MeshStandardMaterial({
      map: this.makeCanvasTexture('#3a2a20'),
      roughness: 0.95,
    });
    const roofMat = new THREE.MeshStandardMaterial({ color: 0x1a0f0a, roughness: 1 });
    const darkWoodMat = new THREE.MeshStandardMaterial({ color: 0x2a1a10, roughness: 0.9 });

    // main building – slightly more complex shape
    const house = new THREE.Mesh(new THREE.BoxGeometry(6.5, 5, 4.5), wallMat);
    house.position.y = 2.5;
    house.castShadow = true;
    group.add(house);

    // second floor extension
    const secondFloor = new THREE.Mesh(new THREE.BoxGeometry(5.0, 2.5, 3.8), wallMat);
    secondFloor.position.y = 5.5;
    secondFloor.castShadow = true;
    group.add(secondFloor);

    // pitched roof – main
    const roof = new THREE.Mesh(new THREE.ConeGeometry(4.8, 2.5, 4), roofMat);
    roof.position.y = 7.75;
    roof.rotation.y = Math.PI / 4;
    roof.castShadow = true;
    group.add(roof);

    // porch overhang
    const porchRoof = new THREE.Mesh(new THREE.BoxGeometry(4.0, 0.15, 1.8), roofMat);
    porchRoof.position.set(0, 3.0, 3.0);
    porchRoof.rotation.x = 0.1;
    group.add(porchRoof);

    // porch columns
    for (const dx of [-1.5, 1.5]) {
      const col = new THREE.Mesh(new THREE.CylinderGeometry(0.08, 0.1, 3.0, 8), darkWoodMat);
      col.position.set(dx, 1.5, 3.0);
      col.castShadow = true;
      group.add(col);
    }

    // tower/turret – taller with more detail
    const turret = new THREE.Mesh(new THREE.CylinderGeometry(0.8, 0.95, 8, 8), wallMat);
    turret.position.set(2.8, 4.0, -1.5);
    turret.castShadow = true;
    group.add(turret);

    const turretRoof = new THREE.Mesh(new THREE.ConeGeometry(1.1, 1.8, 8), roofMat);
    turretRoof.position.set(2.8, 8.5, -1.5);
    group.add(turretRoof);

    // turret windows (narrow slits)
    const turretWinMat = new THREE.MeshStandardMaterial({ color: 0x112211, emissive: 0x00ff00, emissiveIntensity: 0.2 });
    for (let i = 0; i < 4; i++) {
      const angle = (i / 4) * Math.PI * 2;
      const tw = new THREE.Mesh(new THREE.BoxGeometry(0.15, 0.5, 0.05), turretWinMat);
      tw.position.set(2.8 + Math.cos(angle) * 0.85, 5.5, -1.5 + Math.sin(angle) * 0.85);
      tw.lookAt(2.8, 5.5, -1.5);
      group.add(tw);
    }

    // glowing green windows – main building
    const windowMat = new THREE.MeshStandardMaterial({
      color: 0x112211,
      emissive: 0x00ff00,
      emissiveIntensity: 0.3,
    });

    // ground floor windows
    const windowPositions = [
      { x: -1.5, y: 1.5, z: 2.26 },
      { x: 1.5, y: 1.5, z: 2.26 },
    ];
    // first floor windows
    const firstFloorWindows = [
      { x: -1.5, y: 3.5, z: 2.26 },
      { x: 0, y: 3.5, z: 2.26 },
      { x: 1.5, y: 3.5, z: 2.26 },
    ];
    // second floor windows
    const secondFloorWindows = [
      { x: -1.0, y: 5.5, z: 1.91 },
      { x: 1.0, y: 5.5, z: 1.91 },
    ];

    [...windowPositions, ...firstFloorWindows, ...secondFloorWindows].forEach(wp => {
      const w = new THREE.Mesh(new THREE.BoxGeometry(0.5, 0.7, 0.05), windowMat);
      w.position.set(wp.x, wp.y, wp.z);
      group.add(w);

      // window frame
      const frameMat = new THREE.MeshStandardMaterial({ color: 0x2a1a10, roughness: 0.9 });
      const frame = new THREE.Mesh(new THREE.BoxGeometry(0.6, 0.8, 0.03), frameMat);
      frame.position.set(wp.x, wp.y, wp.z + 0.02);
      group.add(frame);
    });

    // front door
    const doorMat = new THREE.MeshStandardMaterial({ color: 0x1a0a05, roughness: 0.9 });
    const door = new THREE.Mesh(new THREE.BoxGeometry(0.8, 1.5, 0.08), doorMat);
    door.position.set(0, 0.75, 2.26);
    group.add(door);

    // door arch
    const archMat = new THREE.MeshStandardMaterial({ color: 0x3a2a1a, roughness: 0.9 });
    const arch = new THREE.Mesh(new THREE.TorusGeometry(0.4, 0.06, 8, 12, Math.PI), archMat);
    arch.position.set(0, 1.5, 2.28);
    group.add(arch);

    // graveyard fence – improved
    const fenceMat = new THREE.MeshStandardMaterial({ color: 0x2a2a2a, metalness: 0.7, roughness: 0.5 });
    for (let i = -4; i <= 4; i++) {
      const post = new THREE.Mesh(new THREE.CylinderGeometry(0.04, 0.04, 1.2, 8), fenceMat);
      post.position.set(i * 0.8, 0.6, 3.8);
      group.add(post);

      // pointed tops
      const point = new THREE.Mesh(new THREE.ConeGeometry(0.06, 0.15, 6), fenceMat);
      point.position.set(i * 0.8, 1.25, 3.8);
      group.add(point);
    }
    const fenceRail = new THREE.Mesh(new THREE.BoxGeometry(7, 0.05, 0.05), fenceMat);
    fenceRail.position.set(0, 1.0, 3.8);
    group.add(fenceRail);
    const fenceRail2 = new THREE.Mesh(new THREE.BoxGeometry(7, 0.05, 0.05), fenceMat);
    fenceRail2.position.set(0, 0.4, 3.8);
    group.add(fenceRail2);

    // gravestones
    const graveMat = new THREE.MeshStandardMaterial({ color: 0x666666, roughness: 0.9 });
    for (const gx of [-2.5, -1.0, 0.5, 2.0]) {
      const grave = new THREE.Mesh(new THREE.BoxGeometry(0.4, 0.6, 0.12), graveMat);
      grave.position.set(gx, 0.3, 4.5);
      grave.rotation.y = (secureRandom() - 0.5) * 0.3;
      grave.rotation.z = (secureRandom() - 0.5) * 0.15;
      group.add(grave);

      // rounded top
      const graveTop = new THREE.Mesh(new THREE.SphereGeometry(0.2, 8, 6, 0, Math.PI * 2, 0, Math.PI / 2), graveMat);
      graveTop.position.set(gx, 0.6, 4.5);
      group.add(graveTop);
    }

    // dead tree – improved
    const deadTreeMat = new THREE.MeshStandardMaterial({ color: 0x3a2a1a, roughness: 1 });
    const deadTrunk = new THREE.Mesh(new THREE.CylinderGeometry(0.12, 0.25, 3.5, 8), deadTreeMat);
    deadTrunk.position.set(-3.8, 1.75, 2);
    deadTrunk.rotation.z = 0.08;
    group.add(deadTrunk);

    // bare branches – more of them
    for (let b = 0; b < 6; b++) {
      const branch = new THREE.Mesh(new THREE.CylinderGeometry(0.02, 0.05, 1.4, 6), deadTreeMat);
      branch.position.set(-3.8 + (secureRandom() - 0.5) * 0.8, 3.0 + b * 0.25, 2 + (secureRandom() - 0.5) * 0.5);
      branch.rotation.z = (secureRandom() - 0.5) * 1.2;
      branch.rotation.x = (secureRandom() - 0.5) * 0.4;
      group.add(branch);
    }

    // cobwebs (thin stretched triangles on corners)
    const cobwebMat = new THREE.MeshBasicMaterial({ color: 0xcccccc, transparent: true, opacity: 0.2, side: THREE.DoubleSide });
    const cobweb = new THREE.Mesh(new THREE.PlaneGeometry(1.0, 1.0), cobwebMat);
    cobweb.position.set(3.26, 4.5, 1.5);
    cobweb.rotation.y = Math.PI / 2;
    group.add(cobweb);

    // bats on roof
    const batMat = new THREE.MeshStandardMaterial({ color: 0x111111 });
    for (let i = 0; i < 3; i++) {
      const batBody = new THREE.Mesh(new THREE.SphereGeometry(0.08, 6, 6), batMat);
      batBody.position.set(-1 + i * 1.5, 7.5 + secureRandom() * 1.5, -0.5 + secureRandom());
      group.add(batBody);
      // wings
      for (const side of [-1, 1]) {
        const wing = new THREE.Mesh(new THREE.PlaneGeometry(0.25, 0.1), batMat);
        wing.position.set(batBody.position.x + side * 0.15, batBody.position.y, batBody.position.z);
        wing.rotation.z = side * 0.5;
        group.add(wing);
      }
    }

    // eerie glow at entrance
    const glowMat = new THREE.MeshBasicMaterial({ color: 0x22ff22, transparent: true, opacity: 0.15 });
    const glow = new THREE.Mesh(new THREE.SphereGeometry(1.0, 12, 12), glowMat);
    glow.position.set(0, 0.5, 2.5);
    group.add(glow);
  }

  private createWaterRide(group: THREE.Group): void {
    const stoneMat = new THREE.MeshPhysicalMaterial({ color: 0x707070, roughness: 0.85, metalness: 0.05 });
    const waterMat = new THREE.MeshPhysicalMaterial({
      color: 0x2080cc,
      metalness: 0.12,
      roughness: 0.02,
      clearcoat: 0.7,
      clearcoatRoughness: 0.05,
      transparent: true,
      opacity: 0.88,
    });
    const slideMat = new THREE.MeshPhysicalMaterial({
      color: 0xeeeeee,
      roughness: 0.3,
      metalness: 0.25,
      clearcoat: 0.6,
    });

    // splash pool – thicker walls
    const pool = new THREE.Mesh(new THREE.CylinderGeometry(3.5, 3.5, 0.8, 48), stoneMat);
    pool.position.y = 0.4;
    pool.receiveShadow = true;
    group.add(pool);

    // pool lip – textured
    const lip = new THREE.Mesh(new THREE.TorusGeometry(3.4, 0.18, 12, 48), stoneMat);
    lip.position.y = 0.8;
    lip.rotation.x = Math.PI / 2;
    group.add(lip);

    // pool inner lip
    const innerLip = new THREE.Mesh(new THREE.TorusGeometry(2.8, 0.08, 8, 48),
      new THREE.MeshStandardMaterial({ color: 0x5a5a5a, roughness: 0.9 }));
    innerLip.position.y = 0.82;
    innerLip.rotation.x = Math.PI / 2;
    group.add(innerLip);

    // animated water surface
    const waterGeo = new THREE.CircleGeometry(3.0, 80);
    const water = new THREE.Mesh(waterGeo, waterMat);
    water.rotation.x = -Math.PI / 2;
    water.position.y = 0.75;
    water.name = 'lakeWater';
    group.add(water);

    // splash effect (small sphere particles at base of slide)
    const splashMat = new THREE.MeshBasicMaterial({ color: 0xaaddff, transparent: true, opacity: 0.5 });
    for (let i = 0; i < 6; i++) {
      const splash = new THREE.Mesh(new THREE.SphereGeometry(0.12 + secureRandom() * 0.1, 8, 8), splashMat);
      splash.position.set(1.2 + (secureRandom() - 0.5) * 0.8, 0.9 + secureRandom() * 0.5, 1.5 + (secureRandom() - 0.5) * 0.5);
      group.add(splash);
    }

    // water slide (tube) – smoother, more control points
    const slideCurve = new THREE.CatmullRomCurve3([
      new THREE.Vector3(-1.5, 6.0, -2.5),
      new THREE.Vector3(-1.2, 5.0, -2.0),
      new THREE.Vector3(-0.8, 4.2, -1.5),
      new THREE.Vector3(-0.3, 3.2, -0.8),
      new THREE.Vector3(0.1, 2.5, -0.2),
      new THREE.Vector3(0.5, 1.8, 0.5),
      new THREE.Vector3(1.0, 1.2, 1.2),
      new THREE.Vector3(1.3, 0.9, 1.6),
    ]);
    const slideGeom = new THREE.TubeGeometry(slideCurve, 120, 0.35, 16, false);
    const slide = new THREE.Mesh(slideGeom, slideMat);
    slide.castShadow = true;
    group.add(slide);

    // slide side rails
    const slideRailMat = new THREE.MeshStandardMaterial({ color: 0x0066cc, roughness: 0.4, metalness: 0.3 });
    for (const offset of [-0.4, 0.4]) {
      const railCurve = new THREE.CatmullRomCurve3(
        slideCurve.getPoints(30).map(p => new THREE.Vector3(p.x + offset * 0.5, p.y + 0.3, p.z))
      );
      const railGeo = new THREE.TubeGeometry(railCurve, 60, 0.04, 8, false);
      const rail = new THREE.Mesh(railGeo, slideRailMat);
      group.add(rail);
    }

    // launch platform – elevated with structure
    const platform = new THREE.Mesh(new THREE.BoxGeometry(2.8, 0.4, 2.2), stoneMat);
    platform.position.set(-1.5, 6.0, -2.5);
    platform.castShadow = true;
    group.add(platform);

    // platform support columns
    for (const pos of [{ x: -2.5, z: -3.2 }, { x: -0.5, z: -3.2 }, { x: -2.5, z: -1.8 }, { x: -0.5, z: -1.8 }]) {
      const col = new THREE.Mesh(new THREE.CylinderGeometry(0.12, 0.15, 6, 10), stoneMat);
      col.position.set(pos.x, 3.0, pos.z);
      col.castShadow = true;
      group.add(col);
    }

    // platform railing
    const railMat = new THREE.MeshStandardMaterial({ color: 0x444444, metalness: 0.5 });
    for (let i = 0; i < 6; i++) {
      const post = new THREE.Mesh(new THREE.CylinderGeometry(0.04, 0.04, 1, 8), railMat);
      post.position.set(
        -2.6 + (i * 0.5),
        6.7,
        -3.3
      );
      group.add(post);
    }
    const railBar = new THREE.Mesh(new THREE.BoxGeometry(2.8, 0.06, 0.06), railMat);
    railBar.position.set(-1.5, 7.0, -3.3);
    group.add(railBar);

    // stairs – with handrail
    for (let s = 0; s < 7; s++) {
      const step = new THREE.Mesh(new THREE.BoxGeometry(1.2, 0.18, 0.4), stoneMat);
      step.position.set(-3.5, 0.4 + s * 0.82, -2.5);
      step.castShadow = true;
      group.add(step);
    }
    // stair handrail
    const handrail = new THREE.Mesh(new THREE.BoxGeometry(0.04, 0.04, 6.5), railMat);
    handrail.position.set(-3.0, 3.5, -2.5);
    handrail.rotation.x = -0.82;
    group.add(handrail);

    // boat at top of slide
    const boatMat = new THREE.MeshPhysicalMaterial({ color: 0xffcc00, roughness: 0.4, clearcoat: 0.3 });
    const boat = new THREE.Mesh(new THREE.CapsuleGeometry(0.2, 0.5, 8, 12), boatMat);
    boat.position.set(-1.5, 6.4, -2.5);
    boat.rotation.z = Math.PI / 2;
    boat.castShadow = true;
    group.add(boat);
  }

  private createBumperCars(group: THREE.Group): void {
    const floorMat = new THREE.MeshPhysicalMaterial({ color: 0x2a2a2a, roughness: 0.8, metalness: 0.1 });
    const carColors = [0xff4d4d, 0x4dff88, 0x4da6ff, 0xffe34d, 0xff79ff, 0xff8844, 0x44ffcc];

    // arena floor with shiny surface
    const floor = new THREE.Mesh(new THREE.CylinderGeometry(4.5, 4.5, 0.25, 48), floorMat);
    floor.position.y = 0.125;
    floor.receiveShadow = true;
    group.add(floor);

    // floor pattern rings – more of them
    const ringMat = new THREE.MeshStandardMaterial({ color: 0x3a3a3a, roughness: 0.7, metalness: 0.15 });
    for (const r of [1.5, 2.5, 3.5]) {
      const ring = new THREE.Mesh(new THREE.TorusGeometry(r, 0.06, 8, 48), ringMat);
      ring.position.y = 0.26;
      ring.rotation.x = Math.PI / 2;
      group.add(ring);
    }

    // directional arrows on floor
    const arrowMat = new THREE.MeshStandardMaterial({ color: 0xffcc00, emissive: 0xffaa00, emissiveIntensity: 0.15 });
    for (let i = 0; i < 8; i++) {
      const angle = (i / 8) * Math.PI * 2;
      const arrow = new THREE.Mesh(new THREE.ConeGeometry(0.15, 0.4, 3), arrowMat);
      arrow.position.set(Math.cos(angle) * 3.0, 0.27, Math.sin(angle) * 3.0);
      arrow.rotation.x = Math.PI / 2;
      arrow.rotation.z = -angle;
      group.add(arrow);
    }

    // bumper cars – more detailed
    for (let i = 0; i < 7; i++) {
      const angle = (i / 7) * Math.PI * 2 + secureRandom() * 0.4;
      const dist = 1.2 + secureRandom() * 2.0;
      const carMat = new THREE.MeshPhysicalMaterial({
        color: carColors[i],
        roughness: 0.2,
        metalness: 0.3,
        clearcoat: 0.5,
        clearcoatRoughness: 0.05,
      });

      const cx = Math.cos(angle) * dist;
      const cz = Math.sin(angle) * dist;

      // car body – egg shape
      const car = new THREE.Mesh(new THREE.SphereGeometry(0.6, 16, 16), carMat);
      car.position.set(cx, 0.5, cz);
      car.scale.set(1, 0.5, 0.85);
      car.castShadow = true;
      group.add(car);

      // car windshield
      const glassMat = new THREE.MeshPhysicalMaterial({
        color: 0x88ccff, roughness: 0.05, transparent: true, opacity: 0.4, clearcoat: 1.0
      });
      const windshield = new THREE.Mesh(new THREE.PlaneGeometry(0.5, 0.25), glassMat);
      windshield.position.set(cx + Math.cos(angle) * 0.35, 0.55, cz + Math.sin(angle) * 0.35);
      windshield.lookAt(cx + Math.cos(angle) * 2, 0.55, cz + Math.sin(angle) * 2);
      group.add(windshield);

      // bumper ring
      const bumper = new THREE.Mesh(
        new THREE.TorusGeometry(0.6, 0.07, 8, 20),
        new THREE.MeshPhysicalMaterial({ color: 0x222222, roughness: 0.7, metalness: 0.3 })
      );
      bumper.position.set(cx, 0.35, cz);
      bumper.rotation.x = Math.PI / 2;
      group.add(bumper);

      // steering wheel
      const steeringMat = new THREE.MeshStandardMaterial({ color: 0x111111 });
      const steering = new THREE.Mesh(new THREE.TorusGeometry(0.1, 0.015, 6, 12), steeringMat);
      steering.position.set(cx, 0.55, cz);
      group.add(steering);

      // antenna pole with spark collector
      const pole = new THREE.Mesh(new THREE.CylinderGeometry(0.02, 0.02, 1.8, 6), new THREE.MeshStandardMaterial({ color: 0x888888, metalness: 0.5 }));
      pole.position.set(cx, 1.3, cz);
      group.add(pole);

      // spark collector disc at top
      const collector = new THREE.Mesh(new THREE.CylinderGeometry(0.15, 0.15, 0.03, 12),
        new THREE.MeshStandardMaterial({ color: 0x666666, metalness: 0.7, roughness: 0.3 }));
      collector.position.set(cx, 2.2, cz);
      group.add(collector);
    }

    // arena fence – with neon effect
    const fenceMat = new THREE.MeshPhysicalMaterial({ color: 0x666666, metalness: 0.5, roughness: 0.4 });
    const fence = new THREE.Mesh(new THREE.TorusGeometry(4.7, 0.1, 10, 80), fenceMat);
    fence.position.y = 0.9;
    fence.rotation.x = Math.PI / 2;
    group.add(fence);

    // neon strip on fence
    const neonMat = new THREE.MeshStandardMaterial({ color: 0x00ff88, emissive: 0x00ff88, emissiveIntensity: 0.5 });
    const neon = new THREE.Mesh(new THREE.TorusGeometry(4.75, 0.03, 6, 80), neonMat);
    neon.position.y = 1.1;
    neon.rotation.x = Math.PI / 2;
    group.add(neon);

    // overhead grid structure – with more detail
    const gridMat = new THREE.MeshPhysicalMaterial({ color: 0x555555, metalness: 0.5, roughness: 0.5 });
    const gridH = 3.5;
    for (let i = 0; i < 6; i++) {
      const angle = (i / 6) * Math.PI * 2;
      const pillar = new THREE.Mesh(new THREE.CylinderGeometry(0.1, 0.14, gridH, 10), gridMat);
      pillar.position.set(Math.cos(angle) * 4.5, gridH / 2, Math.sin(angle) * 4.5);
      pillar.castShadow = true;
      group.add(pillar);
    }

    // overhead disc (electrified ceiling)
    const overheadMat = new THREE.MeshPhysicalMaterial({
      color: 0x444444, roughness: 0.7, metalness: 0.4,
    });
    const overhead = new THREE.Mesh(new THREE.CylinderGeometry(4.5, 4.5, 0.08, 48), overheadMat);
    overhead.position.y = gridH;
    group.add(overhead);

    // overhead grid lines
    for (let i = 0; i < 8; i++) {
      const angle = (i / 8) * Math.PI * 2;
      const gridLine = new THREE.Mesh(new THREE.BoxGeometry(9.0, 0.03, 0.03), gridMat);
      gridLine.position.y = gridH - 0.05;
      gridLine.rotation.y = angle;
      group.add(gridLine);
    }

    // electric sparks (emissive spots on ceiling)
    const sparkMat = new THREE.MeshStandardMaterial({ color: 0x88ccff, emissive: 0x4488ff, emissiveIntensity: 0.8 });
    for (let i = 0; i < 5; i++) {
      const spark = new THREE.Mesh(new THREE.SphereGeometry(0.06, 6, 6), sparkMat);
      spark.position.set((secureRandom() - 0.5) * 6, gridH - 0.08, (secureRandom() - 0.5) * 6);
      group.add(spark);
    }
  }

  private createTrainRide(group: THREE.Group): void {
    const trackMat = new THREE.MeshStandardMaterial({ color: 0x6b3f1f, roughness: 0.8 });
    const metalMat = new THREE.MeshPhysicalMaterial({ color: 0x444444, metalness: 0.6, roughness: 0.4, clearcoat: 0.15 });

    // track oval
    const trackCurve = new THREE.EllipseCurve(0, 0, 4.5, 3.3);
    const trackPoints = trackCurve.getPoints(300);

    // double rails
    const pts3 = trackPoints.map(p => new THREE.Vector3(p.x, 0.1, p.y));
    const curve3d = new THREE.CatmullRomCurve3(pts3, true);

    for (const offset of [-0.15, 0.15]) {
      const railPts = trackPoints.map(p => {
        const len = Math.sqrt(p.x * p.x + p.y * p.y);
        const nx = p.x / len;
        const ny = p.y / len;
        return new THREE.Vector3(p.x + ny * offset, 0.12, p.y - nx * offset);
      });
      const railCurve = new THREE.CatmullRomCurve3(railPts, true);
      const railGeo = new THREE.TubeGeometry(railCurve, 250, 0.03, 8, true);
      const rail = new THREE.Mesh(railGeo, metalMat);
      rail.castShadow = true;
      group.add(rail);
    }

    // sleepers – more of them
    for (let i = 0; i < 55; i++) {
      const t = i / 55;
      const p = curve3d.getPointAt(t);
      const tangent = curve3d.getTangentAt(t);
      const sleeper = new THREE.Mesh(new THREE.BoxGeometry(0.7, 0.05, 0.12), trackMat);
      sleeper.position.set(p.x, 0.05, p.z);
      sleeper.rotation.y = Math.atan2(tangent.x, tangent.z);
      sleeper.receiveShadow = true;
      group.add(sleeper);
    }

    // gravel bed under track
    const gravelMat = new THREE.MeshStandardMaterial({ color: 0x8a7a6a, roughness: 1.0 });
    const gravelPts = trackPoints.map(p => new THREE.Vector3(p.x, 0.02, p.y));
    const gravelCurve = new THREE.CatmullRomCurve3(gravelPts, true);
    const gravelGeo = new THREE.TubeGeometry(gravelCurve, 200, 0.25, 6, true);
    const gravel = new THREE.Mesh(gravelGeo, gravelMat);
    gravel.scale.y = 0.2;
    group.add(gravel);

    // locomotive (detailed steam engine)
    const engineMat = new THREE.MeshPhysicalMaterial({ color: 0x1a6b1a, roughness: 0.3, metalness: 0.3, clearcoat: 0.4 });

    // boiler
    const boiler = new THREE.Mesh(new THREE.CylinderGeometry(0.4, 0.38, 1.3, 16), engineMat);
    boiler.position.set(4.5, 0.6, 0);
    boiler.rotation.z = Math.PI / 2;
    boiler.castShadow = true;
    group.add(boiler);

    // boiler front plate
    const frontPlate = new THREE.Mesh(new THREE.CircleGeometry(0.4, 16), metalMat);
    frontPlate.position.set(5.15, 0.6, 0);
    frontPlate.rotation.y = Math.PI / 2;
    group.add(frontPlate);

    // cab
    const cab = new THREE.Mesh(new THREE.BoxGeometry(0.9, 0.9, 0.9), engineMat);
    cab.position.set(3.75, 0.7, 0);
    cab.castShadow = true;
    group.add(cab);

    // cab roof
    const cabRoof = new THREE.Mesh(new THREE.BoxGeometry(1.1, 0.08, 1.1),
      new THREE.MeshStandardMaterial({ color: 0x0d4d0d, roughness: 0.6 }));
    cabRoof.position.set(3.75, 1.2, 0);
    group.add(cabRoof);

    // cab windows
    const cabWinMat = new THREE.MeshPhysicalMaterial({ color: 0x88ccff, transparent: true, opacity: 0.5, clearcoat: 0.8 });
    for (const dz of [-0.46, 0.46]) {
      const win = new THREE.Mesh(new THREE.PlaneGeometry(0.5, 0.35), cabWinMat);
      win.position.set(3.75, 0.85, dz);
      win.rotation.y = dz > 0 ? 0 : Math.PI;
      group.add(win);
    }

    // chimney – tapered
    const chimney = new THREE.Mesh(
      new THREE.CylinderGeometry(0.08, 0.14, 0.6, 10),
      new THREE.MeshStandardMaterial({ color: 0x111111, roughness: 0.8 })
    );
    chimney.position.set(5.0, 1.2, 0);
    group.add(chimney);

    // chimney cap
    const chimneyTop = new THREE.Mesh(new THREE.CylinderGeometry(0.14, 0.08, 0.1, 10),
      new THREE.MeshStandardMaterial({ color: 0x111111 }));
    chimneyTop.position.set(5.0, 1.55, 0);
    group.add(chimneyTop);

    // smoke puffs – more realistic
    const smokeMat = new THREE.MeshBasicMaterial({ color: 0xcccccc, transparent: true, opacity: 0.35 });
    for (let s = 0; s < 5; s++) {
      const puff = new THREE.Mesh(new THREE.SphereGeometry(0.12 + s * 0.08, 8, 8), smokeMat.clone());
      (puff.material as THREE.MeshBasicMaterial).opacity = 0.35 - s * 0.06;
      puff.position.set(5.0 + (secureRandom() - 0.5) * 0.2, 1.7 + s * 0.35, (secureRandom() - 0.5) * 0.3);
      group.add(puff);
    }

    // cowcatcher / pilot
    const cowcatcherMat = new THREE.MeshStandardMaterial({ color: 0x222222, metalness: 0.4, roughness: 0.6 });
    const cowcatcher = new THREE.Mesh(new THREE.BoxGeometry(0.15, 0.25, 0.7), cowcatcherMat);
    cowcatcher.position.set(5.2, 0.25, 0);
    group.add(cowcatcher);

    // headlamp
    const lampMat = new THREE.MeshStandardMaterial({ color: 0xffee88, emissive: 0xffdd44, emissiveIntensity: 0.6 });
    const headlamp = new THREE.Mesh(new THREE.SphereGeometry(0.08, 8, 8), lampMat);
    headlamp.position.set(5.18, 0.8, 0);
    group.add(headlamp);

    // locomotive wheels
    const wheelMat = new THREE.MeshStandardMaterial({ color: 0x222222, metalness: 0.4 });
    for (const dx of [4.0, 4.5, 5.0]) {
      for (const dz of [-0.42, 0.42]) {
        const wheel = new THREE.Mesh(new THREE.CylinderGeometry(0.15, 0.15, 0.05, 12), wheelMat);
        wheel.position.set(dx, 0.15, dz);
        wheel.rotation.x = Math.PI / 2;
        group.add(wheel);
      }
    }

    // passenger cars – more detailed
    const carColors = [0xcc3333, 0x3366cc, 0xcc9933];
    for (let i = 0; i < 3; i++) {
      const carMat = new THREE.MeshPhysicalMaterial({ color: carColors[i], roughness: 0.4, metalness: 0.15, clearcoat: 0.3 });
      const car = new THREE.Mesh(new THREE.BoxGeometry(0.95, 0.55, 0.7), carMat);
      car.position.set(3.75 - (i + 1) * 1.1, 0.5, 0);
      car.castShadow = true;
      group.add(car);

      // car roof
      const carRoof = new THREE.Mesh(new THREE.BoxGeometry(1.0, 0.06, 0.75),
        new THREE.MeshStandardMaterial({ color: carColors[i] * 0.7, roughness: 0.6 }));
      carRoof.position.set(3.75 - (i + 1) * 1.1, 0.8, 0);
      group.add(carRoof);

      // windows on each car
      for (const dz of [-0.36, 0.36]) {
        for (let w = 0; w < 2; w++) {
          const win = new THREE.Mesh(new THREE.PlaneGeometry(0.25, 0.2), cabWinMat);
          win.position.set(3.75 - (i + 1) * 1.1 + (w - 0.5) * 0.35, 0.6, dz);
          win.rotation.y = dz > 0 ? 0 : Math.PI;
          group.add(win);
        }
      }

      // wheels
      for (const dz of [-0.38, 0.38]) {
        for (const dx2 of [-0.3, 0.3]) {
          const wheel = new THREE.Mesh(new THREE.CylinderGeometry(0.1, 0.1, 0.04, 10), wheelMat);
          wheel.position.set(3.75 - (i + 1) * 1.1 + dx2, 0.18, dz);
          wheel.rotation.x = Math.PI / 2;
          group.add(wheel);
        }
      }

      // coupling between cars
      if (i < 2) {
        const coupling = new THREE.Mesh(new THREE.BoxGeometry(0.15, 0.06, 0.06), metalMat);
        coupling.position.set(3.75 - (i + 1) * 1.1 - 0.55, 0.35, 0);
        group.add(coupling);
      }
    }

    // station platform
    const platMat = new THREE.MeshStandardMaterial({ color: 0x8a6a4a, roughness: 0.85 });
    const platform = new THREE.Mesh(new THREE.BoxGeometry(3.5, 0.3, 1.5), platMat);
    platform.position.set(0, 0.15, -4.0);
    platform.receiveShadow = true;
    group.add(platform);

    // station canopy
    const canopyMat = new THREE.MeshStandardMaterial({ color: 0xcc3333, roughness: 0.6 });
    const canopy = new THREE.Mesh(new THREE.BoxGeometry(3.8, 0.08, 1.8), canopyMat);
    canopy.position.set(0, 2.5, -4.0);
    group.add(canopy);

    // canopy pillars
    for (const dx of [-1.5, 1.5]) {
      const pillar = new THREE.Mesh(new THREE.CylinderGeometry(0.06, 0.06, 2.2, 8), metalMat);
      pillar.position.set(dx, 1.4, -4.5);
      group.add(pillar);
    }
  }

  private createSwingRide(group: THREE.Group): void {
    const poleMat = new THREE.MeshPhysicalMaterial({ color: 0xffd700, metalness: 0.5, roughness: 0.2, clearcoat: 0.4 });
    const topMat = new THREE.MeshPhysicalMaterial({ color: 0xff5555, roughness: 0.4, metalness: 0.1, clearcoat: 0.3 });
    const chainMat = new THREE.MeshPhysicalMaterial({ color: 0x999999, metalness: 0.6, roughness: 0.3 });

    // center pole – tapered with rings
    const centerPole = new THREE.Mesh(new THREE.CylinderGeometry(0.25, 0.4, 9, 16), poleMat);
    centerPole.position.y = 4.5;
    centerPole.castShadow = true;
    group.add(centerPole);

    // decorative rings on pole
    for (const h of [1.5, 3.0, 4.5, 6.0, 7.5]) {
      const ring = new THREE.Mesh(new THREE.TorusGeometry(0.35, 0.04, 8, 16), poleMat);
      ring.position.y = h;
      ring.rotation.x = Math.PI / 2;
      group.add(ring);
    }

    // decorative top disc – multi-layered
    const topDisc = new THREE.Mesh(new THREE.CylinderGeometry(3.8, 3.6, 0.8, 32), topMat);
    topDisc.position.y = 8.8;
    topDisc.castShadow = true;
    group.add(topDisc);

    // top disc – lower ring with alternating colors
    const topDiscLower = new THREE.Mesh(new THREE.CylinderGeometry(4.0, 3.8, 0.3, 32),
      new THREE.MeshPhysicalMaterial({ color: 0xffdd44, roughness: 0.4, clearcoat: 0.3 }));
    topDiscLower.position.y = 8.35;
    group.add(topDiscLower);

    // top roof cone
    const topCone = new THREE.Mesh(new THREE.ConeGeometry(2.5, 1.5, 24),
      new THREE.MeshPhysicalMaterial({ color: 0xff3333, roughness: 0.4, clearcoat: 0.2 }));
    topCone.position.y = 10.0;
    group.add(topCone);

    // finial
    const finialMat = new THREE.MeshPhysicalMaterial({ color: 0xffd700, metalness: 0.7, roughness: 0.15, clearcoat: 0.5 });
    const finial = new THREE.Mesh(new THREE.SphereGeometry(0.18, 12, 12), finialMat);
    finial.position.y = 10.9;
    group.add(finial);

    // top trim
    const trimMat = new THREE.MeshPhysicalMaterial({ color: 0xffd700, metalness: 0.6, roughness: 0.2, clearcoat: 0.4 });
    const topTrim = new THREE.Mesh(new THREE.TorusGeometry(3.8, 0.08, 10, 32), trimMat);
    topTrim.position.y = 9.2;
    topTrim.rotation.x = Math.PI / 2;
    group.add(topTrim);

    const topTrim2 = new THREE.Mesh(new THREE.TorusGeometry(4.0, 0.06, 10, 32), trimMat);
    topTrim2.position.y = 8.2;
    topTrim2.rotation.x = Math.PI / 2;
    group.add(topTrim2);

    // hanging swings – more of them with chain links
    const seatColors = [0x4466ff, 0xff4444, 0x44cc44, 0xffcc00, 0xff66cc,
      0x44cccc, 0xcc8844, 0x8844cc, 0xff8844, 0x44ff88, 0x6688ff, 0xff6666];
    for (let i = 0; i < 12; i++) {
      const angle = (i / 12) * Math.PI * 2;
      const swingAngle = 0.22; // tilted outward

      // double chains
      for (const cOff of [-0.08, 0.08]) {
        const chain = new THREE.Mesh(new THREE.CylinderGeometry(0.015, 0.015, 3.2, 6), chainMat);
        chain.position.set(
          Math.cos(angle) * (3.4 + cOff),
          7.0,
          Math.sin(angle) * (3.4 + cOff)
        );
        chain.rotation.z = Math.cos(angle) * swingAngle;
        chain.rotation.x = Math.sin(angle) * swingAngle;
        group.add(chain);
      }

      // chain links visualization (small spheres along chain)
      for (let cl = 0; cl < 5; cl++) {
        const linkY = 8.2 - cl * 0.6;
        const linkR = 3.4 + (8.5 - linkY) * 0.12;
        const link = new THREE.Mesh(new THREE.SphereGeometry(0.025, 6, 6), chainMat);
        link.position.set(Math.cos(angle) * linkR, linkY, Math.sin(angle) * linkR);
        group.add(link);
      }

      // seat – more detailed
      const seatMat = new THREE.MeshPhysicalMaterial({
        color: seatColors[i],
        roughness: 0.4,
        metalness: 0.1,
        clearcoat: 0.4,
      });
      const seat = new THREE.Mesh(new THREE.BoxGeometry(0.45, 0.1, 0.45), seatMat);
      seat.position.set(
        Math.cos(angle) * 4.0,
        5.3,
        Math.sin(angle) * 4.0
      );
      seat.castShadow = true;
      group.add(seat);

      // seat back
      const seatBack = new THREE.Mesh(new THREE.BoxGeometry(0.4, 0.35, 0.06), seatMat);
      seatBack.position.set(
        Math.cos(angle) * 3.8,
        5.55,
        Math.sin(angle) * 3.8
      );
      seatBack.lookAt(0, 5.55, 0);
      group.add(seatBack);

      // foot rest
      const footRest = new THREE.Mesh(new THREE.BoxGeometry(0.35, 0.04, 0.15),
        new THREE.MeshStandardMaterial({ color: 0x333333 }));
      footRest.position.set(
        Math.cos(angle) * 4.15,
        5.05,
        Math.sin(angle) * 4.15
      );
      group.add(footRest);
    }

    // lights on top disc
    const bulbMat = new THREE.MeshStandardMaterial({
      color: 0xffee88,
      emissive: 0xffdd44,
      emissiveIntensity: 0.6,
    });
    for (let i = 0; i < 16; i++) {
      const angle = (i / 16) * Math.PI * 2;
      const bulb = new THREE.Mesh(new THREE.SphereGeometry(0.05, 8, 8), bulbMat);
      bulb.position.set(Math.cos(angle) * 3.9, 8.35, Math.sin(angle) * 3.9);
      group.add(bulb);
    }

    // lights on lower edge
    for (let i = 0; i < 20; i++) {
      const angle = (i / 20) * Math.PI * 2;
      const bulb = new THREE.Mesh(new THREE.SphereGeometry(0.04, 6, 6), bulbMat);
      bulb.position.set(Math.cos(angle) * 4.0, 8.18, Math.sin(angle) * 4.0);
      group.add(bulb);
    }

    // base support struts
    const strutMat = new THREE.MeshStandardMaterial({ color: 0x666666, metalness: 0.4, roughness: 0.5 });
    for (let i = 0; i < 3; i++) {
      const angle = (i / 3) * Math.PI * 2;
      const strut = new THREE.Mesh(new THREE.CylinderGeometry(0.08, 0.12, 2.0, 8), strutMat);
      strut.position.set(Math.cos(angle) * 1.2, 1.0, Math.sin(angle) * 1.2);
      strut.rotation.z = Math.cos(angle) * 0.3;
      strut.rotation.x = Math.sin(angle) * 0.3;
      group.add(strut);
    }
  }

  private createDefaultMarker(group: THREE.Group): void {
    // Carnival show pavilion / kiosk for generic "OTHER" attractions

    // --- Octagonal base platform ---
    const baseMat = new THREE.MeshPhysicalMaterial({ color: 0xdec9a0, roughness: 0.7, metalness: 0.05, clearcoat: 0.1 });
    const base = new THREE.Mesh(new THREE.CylinderGeometry(4, 4.3, 0.5, 8), baseMat);
    base.position.y = 0.25;
    base.castShadow = true;
    base.receiveShadow = true;
    group.add(base);

    // base edge trim
    const baseEdgeMat = new THREE.MeshPhysicalMaterial({ color: 0xc4a87a, roughness: 0.6, metalness: 0.1 });
    const baseEdge = new THREE.Mesh(new THREE.TorusGeometry(4.15, 0.06, 8, 8), baseEdgeMat);
    baseEdge.position.y = 0.5;
    baseEdge.rotation.x = Math.PI / 2;
    group.add(baseEdge);

    // --- Tent poles (8 around perimeter) – with decorative rings ---
    const poleMat = new THREE.MeshPhysicalMaterial({ color: 0x8b4513, roughness: 0.6, metalness: 0.1, clearcoat: 0.15 });
    const goldAccentMat = new THREE.MeshPhysicalMaterial({ color: 0xffd700, metalness: 0.6, roughness: 0.2, clearcoat: 0.4 });
    for (let i = 0; i < 8; i++) {
      const angle = (i / 8) * Math.PI * 2;
      const pole = new THREE.Mesh(new THREE.CylinderGeometry(0.1, 0.13, 5, 10), poleMat);
      pole.position.set(Math.cos(angle) * 3.5, 3, Math.sin(angle) * 3.5);
      pole.castShadow = true;
      group.add(pole);

      // decorative ring on each pole
      const ring = new THREE.Mesh(new THREE.TorusGeometry(0.14, 0.025, 8, 12), goldAccentMat);
      ring.position.set(Math.cos(angle) * 3.5, 5.3, Math.sin(angle) * 3.5);
      ring.rotation.x = Math.PI / 2;
      group.add(ring);

      // pole base
      const poleBase = new THREE.Mesh(new THREE.CylinderGeometry(0.18, 0.2, 0.2, 10), poleMat);
      poleBase.position.set(Math.cos(angle) * 3.5, 0.6, Math.sin(angle) * 3.5);
      group.add(poleBase);
    }

    // --- Center pole – ornate ---
    const centerPole = new THREE.Mesh(new THREE.CylinderGeometry(0.15, 0.18, 7, 12), poleMat);
    centerPole.position.y = 3.5;
    centerPole.castShadow = true;
    group.add(centerPole);

    // center pole rings
    for (const h of [2.0, 3.5, 5.0]) {
      const ring = new THREE.Mesh(new THREE.TorusGeometry(0.2, 0.03, 8, 12), goldAccentMat);
      ring.position.y = h;
      ring.rotation.x = Math.PI / 2;
      group.add(ring);
    }

    // --- Tent roof (cone) with stripes – higher quality ---
    const roofCanvas = document.createElement('canvas');
    roofCanvas.width = 512;
    roofCanvas.height = 512;
    const rCtx = roofCanvas.getContext('2d')!;
    const stripeColors = ['#cc0000', '#ffffff'];
    const segments = 24;
    for (let i = 0; i < segments; i++) {
      rCtx.beginPath();
      rCtx.moveTo(256, 256);
      rCtx.arc(256, 256, 256, (i / segments) * Math.PI * 2, ((i + 1) / segments) * Math.PI * 2);
      rCtx.closePath();
      rCtx.fillStyle = stripeColors[i % 2];
      rCtx.fill();
    }
    const roofTexture = new THREE.CanvasTexture(roofCanvas);
    roofTexture.colorSpace = THREE.SRGBColorSpace;
    const roofMat = new THREE.MeshPhysicalMaterial({ map: roofTexture, roughness: 0.5, side: THREE.DoubleSide, clearcoat: 0.1 });
    const roof = new THREE.Mesh(new THREE.ConeGeometry(5, 3, 16, 1, true), roofMat);
    roof.position.y = 6.5;
    roof.castShadow = true;
    group.add(roof);

    // roof underside (visible from below)
    const roofUnder = new THREE.Mesh(new THREE.ConeGeometry(4.9, 0.3, 16),
      new THREE.MeshStandardMaterial({ color: 0xaa0000, roughness: 0.7, side: THREE.DoubleSide }));
    roofUnder.position.y = 5.15;
    group.add(roofUnder);

    // --- Roof brim (torus for overhang) ---
    const brimMat = new THREE.MeshPhysicalMaterial({ color: 0xcc0000, roughness: 0.5, clearcoat: 0.2 });
    const brim = new THREE.Mesh(new THREE.TorusGeometry(4.2, 0.18, 10, 32), brimMat);
    brim.position.y = 5.5;
    brim.rotation.x = Math.PI / 2;
    group.add(brim);

    // --- Flag on top ---
    const flagPoleMat = new THREE.MeshStandardMaterial({ color: 0x444444, metalness: 0.3 });
    const flagPole = new THREE.Mesh(new THREE.CylinderGeometry(0.04, 0.04, 1.8, 8), flagPoleMat);
    flagPole.position.y = 8.7;
    group.add(flagPole);

    const flagMat = new THREE.MeshPhysicalMaterial({ color: 0xffcc00, side: THREE.DoubleSide, roughness: 0.5 });
    const flag = new THREE.Mesh(new THREE.PlaneGeometry(0.9, 0.55), flagMat);
    flag.position.set(0.45, 9.3, 0);
    group.add(flag);

    // --- Scalloped valance / bunting around roof edge ---
    const buntingColors = [0xffdd44, 0xff6644, 0x44aaff, 0x44cc44];
    for (let i = 0; i < 20; i++) {
      const angle = (i / 20) * Math.PI * 2;
      const buntingMat = new THREE.MeshPhysicalMaterial({
        color: buntingColors[i % buntingColors.length],
        roughness: 0.4,
        side: THREE.DoubleSide,
        clearcoat: 0.2,
      });
      const bunting = new THREE.Mesh(new THREE.SphereGeometry(0.22, 8, 6), buntingMat);
      bunting.position.set(Math.cos(angle) * 4.2, 5.3, Math.sin(angle) * 4.2);
      bunting.scale.y = 0.5;
      group.add(bunting);
    }

    // --- Decorative lights around poles – with glow ---
    const bulbMat = new THREE.MeshStandardMaterial({
      color: 0xffee88,
      emissive: 0xffdd44,
      emissiveIntensity: 0.7,
    });
    for (let i = 0; i < 16; i++) {
      const angle = (i / 16) * Math.PI * 2;
      const bulb = new THREE.Mesh(new THREE.SphereGeometry(0.08, 8, 8), bulbMat);
      bulb.position.set(Math.cos(angle) * 3.7, 5.35, Math.sin(angle) * 3.7);
      group.add(bulb);
    }

    // string lights between poles
    for (let i = 0; i < 8; i++) {
      const angle = (i / 8) * Math.PI * 2;
      const nextAngle = ((i + 1) / 8) * Math.PI * 2;
      for (let j = 1; j < 4; j++) {
        const t = j / 4;
        const a = angle + (nextAngle - angle) * t;
        const sag = Math.sin(t * Math.PI) * 0.3; // catenary sag
        const stringLight = new THREE.Mesh(new THREE.SphereGeometry(0.05, 6, 6), bulbMat);
        stringLight.position.set(Math.cos(a) * 3.5, 4.8 - sag, Math.sin(a) * 3.5);
        group.add(stringLight);
      }
    }

    // --- Star ornament on top – better ---
    const starMat = new THREE.MeshPhysicalMaterial({
      color: 0xffd700,
      emissive: 0xffaa00,
      emissiveIntensity: 0.5,
      metalness: 0.7,
      roughness: 0.15,
      clearcoat: 0.5,
    });
    const star = new THREE.Mesh(new THREE.OctahedronGeometry(0.35, 0), starMat);
    star.position.y = 9.7;
    star.rotation.y = Math.PI / 4;
    group.add(star);

    // --- Small stage / counter inside ---
    const counterMat = new THREE.MeshPhysicalMaterial({ color: 0x6b4226, roughness: 0.7, clearcoat: 0.1 });
    const counter = new THREE.Mesh(new THREE.BoxGeometry(3, 1.2, 0.5), counterMat);
    counter.position.set(0, 1.1, 3.2);
    counter.castShadow = true;
    group.add(counter);

    // --- Counter top – polished ---
    const topMat = new THREE.MeshPhysicalMaterial({ color: 0xf5deb3, roughness: 0.4, clearcoat: 0.3 });
    const counterTop = new THREE.Mesh(new THREE.BoxGeometry(3.3, 0.1, 0.7), topMat);
    counterTop.position.set(0, 1.75, 3.2);
    group.add(counterTop);

    // prize shelves on back wall
    const shelfMat = new THREE.MeshStandardMaterial({ color: 0x8b6b4a, roughness: 0.8 });
    for (const h of [1.8, 2.8, 3.8]) {
      const shelf = new THREE.Mesh(new THREE.BoxGeometry(2.5, 0.06, 0.4), shelfMat);
      shelf.position.set(0, h, -0.2);
      group.add(shelf);
    }

    // colorful prize objects on shelves
    const prizeColors = [0xff4444, 0x44ff44, 0x4444ff, 0xffff44, 0xff44ff, 0x44ffff];
    for (let s = 0; s < 3; s++) {
      for (let p = 0; p < 4; p++) {
        const prizeMat = new THREE.MeshPhysicalMaterial({
          color: prizeColors[(s * 4 + p) % prizeColors.length],
          roughness: 0.3,
          clearcoat: 0.5,
        });
        const prize = new THREE.Mesh(new THREE.SphereGeometry(0.12, 8, 8), prizeMat);
        prize.position.set(-0.8 + p * 0.55, 1.95 + s, -0.2);
        group.add(prize);
      }
    }

    // entrance curtains (on front sides)
    const curtainMat = new THREE.MeshStandardMaterial({ color: 0xcc0000, roughness: 0.6, side: THREE.DoubleSide });
    for (const dx of [-2.5, 2.5]) {
      const curtain = new THREE.Mesh(new THREE.PlaneGeometry(1.2, 3.0), curtainMat);
      curtain.position.set(dx, 2.5, 3.5);
      curtain.rotation.y = dx > 0 ? -0.3 : 0.3;
      group.add(curtain);
    }
  }
}
