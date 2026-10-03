import { Injectable, computed, signal } from '@angular/core';
import { en } from './i18n/en';
import { fr } from './i18n/fr';

export type Lang = 'en' | 'fr';

const STORAGE_KEY = 'agency-voyage:lang';
const DICTIONARIES: Record<Lang, Record<string, string>> = { en, fr };

@Injectable({ providedIn: 'root' })
export class I18nService {
  readonly lang = signal<Lang>(readStoredLang());

  private readonly dictionary = computed(() => DICTIONARIES[this.lang()]);

  setLang(lang: Lang): void {
    this.lang.set(lang);
    try {
      localStorage.setItem(STORAGE_KEY, lang);
    } catch {
      // ignore - localStorage unavailable
    }
  }

  /** Looks up `key` in the current language, substituting any `{{param}}` placeholders. */
  t(key: string, params?: Record<string, string | number>): string {
    const text = this.dictionary()[key] ?? key;
    if (!params) {
      return text;
    }
    return Object.entries(params).reduce(
      (result, [name, value]) => result.replaceAll(`{{${name}}}`, String(value)),
      text,
    );
  }
}

function readStoredLang(): Lang {
  try {
    return localStorage.getItem(STORAGE_KEY) === 'fr' ? 'fr' : 'en';
  } catch {
    return 'en';
  }
}
