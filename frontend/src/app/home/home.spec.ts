import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HomeComponent } from './home';
import { TranslateModule } from '@ngx-translate/core';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomeComponent, TranslateModule.forRoot()]
    }).compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have promoVideo ViewChild', () => {
    fixture.detectChanges();
    expect(component.promoVideo).toBeDefined();
  });
});

