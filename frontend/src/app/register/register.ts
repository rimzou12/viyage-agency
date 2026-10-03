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
  selector: 'app-register',
  standalone: true,
  imports: [RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './register.html',
  styleUrl: './register.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Register {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  protected readonly i18n = inject(I18nService);

  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);

  protected submit(displayName: string, email: string, password: string): void {
    if (!displayName.trim() || !email.trim() || !password) {
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    this.auth.register(email.trim(), password, displayName.trim()).subscribe({
      next: () => {
        const redirectTo = this.route.snapshot.queryParamMap.get('redirectTo') ?? '/';
        this.router.navigateByUrl(redirectTo);
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.error.set(apiErrorMessage(err, this.i18n.t('register.error')));
      },
    });
  }
}
