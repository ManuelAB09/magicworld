import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TicketType } from './ticket-type';

describe('TicketType', () => {
  let component: TicketType;
  let fixture: ComponentFixture<TicketType>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TicketType]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TicketType);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
