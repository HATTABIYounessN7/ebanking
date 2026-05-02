import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AccountService } from '../../services/account.service';
import { AccountHistory, BankAccount } from '../../models/account.model';
import { forkJoin, Observable } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-accounts',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './accounts.html',
})
export class Accounts implements OnInit {
  private accountService = inject(AccountService);
  private formBuilder = inject(FormBuilder);

  accounts = signal<BankAccount[]>([]);
  selectedAccount = signal<BankAccount | null>(null);
  accountHistory = signal<AccountHistory | null>(null);

  loading = signal(false);
  historyLoading = signal(false);
  errorMessage = signal('');
  successMessage = signal('');
  currentPage = signal(0);
  pageSize = 5;

  searchForm: FormGroup = this.formBuilder.group({
    accountId: ['', Validators.required],
  });

  operationForm: FormGroup = this.formBuilder.group({
    operationType: ['DEBIT', Validators.required],
    amount: [0, [Validators.required, Validators.min(1)]],
    description: ['', Validators.required],
    accountDestination: [''],
  });

  operationType = toSignal(this.operationForm.get('operationType')!.valueChanges, {
    initialValue: this.operationForm.get('operationType')!.value,
  });

  ngOnInit(): void {
    this.loadAccounts();
  }

  loadAccounts() {
    this.loading.set(true);
    this.errorMessage.set('');

    this.accountService.getAccounts().subscribe({
      next: (data) => {
        this.accounts.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Failed to load accounts');
        this.loading.set(false);
      },
    });
  }

  searchAccount() {
    if (this.searchForm.invalid) return;
    const accountId = this.searchForm.value.accountId;
    this.loadAccountHistory(accountId, 0);
  }

  loadAccountHistory(accountId: string, page: number) {
    this.historyLoading.set(true);
    this.errorMessage.set('');

    forkJoin({
      account: this.accountService.getAccount(accountId),
      history: this.accountService.getAccountHistory(accountId, page, this.pageSize),
    }).subscribe({
      next: ({ account, history }) => {
        this.selectedAccount.set(account);
        this.accountHistory.set(history);
        console.log('History: ', history);
        console.log('Account History : ', this.accountHistory());
        this.currentPage.set(page);
        this.historyLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Failed to load account data');
        this.historyLoading.set(false);
      },
    });
  }

  executeOperation() {
    if (this.operationForm.invalid || !this.selectedAccount()) return;

    const { operationType, amount, description, accountDestination } = this.operationForm.value;
    const accountId = this.selectedAccount()!.id;

    this.loading.set(true);
    this.errorMessage.set('');

    let operation$: Observable<any>;

    switch (operationType) {
      case 'DEBIT':
        operation$ = this.accountService.debit({ accountId, amount, description });
        break;
      case 'CREDIT':
        operation$ = this.accountService.credit({ accountId, amount, description });
        break;
      case 'TRANSFER':
        operation$ = this.accountService.transfer({
          accountSource: accountId,
          accountDestination,
          amount,
          description,
        });
        break;
      default:
        return;
    }

    operation$.subscribe({
      next: () => {
        this.loading.set(false);
        this.successMessage.set(`${operationType} operation completed successfully`);
        this.operationForm.reset({ operationType: 'DEBIT', amount: 0 });

        this.loadAccountHistory(accountId, this.currentPage());

        setTimeout(() => this.successMessage.set(''), 3000);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.error?.message ?? 'Operation failed');
      },
    });
  }

  goToPage(page: number) {
    if (!this.selectedAccount()) return;
    this.loadAccountHistory(this.selectedAccount()!.id, page);
  }

  pages = computed(() => {
    const history = this.accountHistory();
    if (!history) return [];
    return Array.from({ length: history.totalPages }, (_, i) => i);
  });
}
