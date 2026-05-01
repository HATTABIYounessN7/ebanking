import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CustomerService } from '../../services/customer.service';
import { Customer } from '../../models/customer.model';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs';

@Component({
  selector: 'app-customers',
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './customers.html',
})
export class Customers implements OnInit {
  private customerService = inject(CustomerService);

  customers: Customer[] = [];
  loading = false;
  errorMessage = '';
  deleteSuccess = '';

  searchControl = new FormControl('');

  ngOnInit() {
    this.loadCustomers();
    this.setupSearch();
  }

  loadCustomers() {
    this.loading = true;
    this.customerService.getCustomers().subscribe({
      next: (data) => {
        this.customers = data;
        this.loading = false;
      },
      error: (_) => {
        this.errorMessage = 'Failed to load customers';
        this.loading = false;
      },
    });
  }

  setupSearch() {
    this.searchControl.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((keyword) => this.customerService.searchCustomers(keyword ?? '')),
      )
      .subscribe({
        next: (data) => (this.customers = data),
        error: () => (this.errorMessage = 'Search failed'),
      });
  }

  deleteCustomer(id: number) {
    if (!confirm('Are you sure you want to delete this customer?')) return;

    this.customerService.deleteCustomer(id).subscribe({
      next: () => {
        this.deleteSuccess = 'Customer deleted successfully';
        this.customers = this.customers.filter((c) => c.id !== id);
        setTimeout(() => (this.deleteSuccess = ''), 3000);
      },
      error: () => (this.errorMessage = 'Failed to delete customer'),
    });
  }
}
