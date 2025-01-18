import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PromoterProfileComponent } from './promoter-profile.component';

describe('PromoterProfileComponent', () => {
  let component: PromoterProfileComponent;
  let fixture: ComponentFixture<PromoterProfileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PromoterProfileComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PromoterProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
