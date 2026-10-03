import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AuthService } from '../core/auth.service';
import { I18nService } from '../core/i18n.service';
import { apiErrorMessage } from '../core/group-booking.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  protected readonly i18n = inject(I18nService);

  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);

  protected submit(email: string, password: string): void {
    if (!email.trim() || !password) {
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    this.auth.login(email.trim(), password).subscribe({
      next: () => {
        const explicitRedirect = this.route.snapshot.queryParamMap.get('redirectTo');
        const redirectTo = explicitRedirect ?? (this.auth.currentUser()?.isAdmin ? '/admin' : '/');
        this.router.navigateByUrl(redirectTo);
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.error.set(apiErrorMessage(err, this.i18n.t('login.error')));
      },
    });
  }
}
