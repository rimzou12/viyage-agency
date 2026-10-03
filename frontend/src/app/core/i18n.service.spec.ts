import { TestBed } from '@angular/core/testing';
import { I18nService } from './i18n.service';
import { en } from './i18n/en';
import { fr } from './i18n/fr';

describe('I18nService', () => {
  beforeEach(() => localStorage.removeItem('agency-voyage:lang'));
  afterEach(() => localStorage.removeItem('agency-voyage:lang'));

  it('defaults to English', () => {
    const service = TestBed.inject(I18nService);
    expect(service.lang()).toBe('en');
    expect(service.t('login.title')).toBe('Welcome back');
  });

  it('switches language and persists the choice', () => {
    const service = TestBed.inject(I18nService);
    service.setLang('fr');

    expect(service.lang()).toBe('fr');
    expect(service.t('login.title')).toBe('Content de vous revoir');
    expect(localStorage.getItem('agency-voyage:lang')).toBe('fr');
  });

  it('reads the persisted language on startup', () => {
    localStorage.setItem('agency-voyage:lang', 'fr');
    TestBed.resetTestingModule();

    const service = TestBed.inject(I18nService);

    expect(service.lang()).toBe('fr');
  });

  it('substitutes {{param}} placeholders', () => {
    const service = TestBed.inject(I18nService);
    expect(service.t('nav.hello', { name: 'Alice' })).toBe('Hi, Alice');
  });

  it('falls back to the key itself when a translation is missing', () => {
    const service = TestBed.inject(I18nService);
    expect(service.t('nonexistent.key')).toBe('nonexistent.key');
  });

  it('keeps the English and French dictionaries in sync (same key set)', () => {
    expect(Object.keys(fr).sort()).toEqual(Object.keys(en).sort());
  });
});
