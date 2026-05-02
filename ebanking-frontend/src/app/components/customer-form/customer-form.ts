import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CustomerService } from '../../services/customer.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-customer-form',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './customer-form.html',
})
export class CustomerForm implements OnInit {
  private formBuilder = inject(FormBuilder);
  private customerService = inject(CustomerService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  form: FormGroup = this.formBuilder.group({
    name: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
  });

  customerId: number | null = null;
  isEditMode = signal(false);
  loading = signal(false);
  errorMessage = '';

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode.set(true);
      this.customerId = +id;
      this.loadCustomer(this.customerId);
    }
  }

  loadCustomer(id: number) {
    this.loading.set(true);
    this.customerService.getCustomer(id).subscribe({
      next: (customer) => {
        this.form.patchValue(customer);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage = 'Failed to load customer';
        this.loading.set(false);
      },
    });
  }

  onSubmit() {
    if (this.form.invalid) return;

    this.loading.set(true);
    const action = this.isEditMode()
      ? this.customerService.updateCustomer(this.customerId!, this.form.value)
      : this.customerService.saveCustomer(this.form.value);

    action.subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/customers']);
      },
      error: (err) => {
        this.errorMessage = err.error?.message ?? 'Operation failed';
        this.loading.set(false);
      },
    });
  }

  get name() {
    return this.form.get('name');
  }
  get email() {
    return this.form.get('email');
  }
}
