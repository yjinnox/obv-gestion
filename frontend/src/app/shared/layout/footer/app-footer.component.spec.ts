import { TestBed } from '@angular/core/testing';
import { AppFooterComponent } from './app-footer.component';

describe('AppFooterComponent', () => {
  it("affiche le copyright avec l'année courante, jamais une année figée", () => {
    TestBed.configureTestingModule({ imports: [AppFooterComponent] });
    const fixture = TestBed.createComponent(AppFooterComponent);
    fixture.detectChanges();

    const texte = (fixture.nativeElement as HTMLElement).textContent?.trim();
    expect(texte).toBe(`Copyright ASSOMA Technology - ${new Date().getFullYear()}`);
  });
});
