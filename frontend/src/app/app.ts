import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { AuthService } from './core/auth.service';
import { I18nService, Lang } from './core/i18n.service';
import { ChatWidget } from './chat-widget/chat-widget';

@Component({
  imports: [RouterOutlet, RouterLink, MatToolbarModule, MatButtonModule, MatIconModule, ChatWidget],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {
  protected readonly auth = inject(AuthService);
  protected readonly i18n = inject(I18nService);
  private readonly router = inject(Router);
  protected readonly currentYear = new Date().getFullYear();

  protected logout(): void {
    this.auth.logout();
    this.router.navigateByUrl('/');
  }

  protected setLang(lang: Lang): void {
    this.i18n.setLang(lang);
  }
}
