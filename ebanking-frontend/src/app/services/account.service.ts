import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment.development';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  AccountHistory,
  BankAccount,
  CreditRequest,
  DebitRequest,
  TransferRequest,
} from '../models/account.model';

@Injectable({
  providedIn: 'root',
})
export class AccountService {
  private apiUrl = `${environment.apiUrl}/accounts`;

  constructor(private http: HttpClient) {}

  getAccounts(): Observable<BankAccount[]> {
    return this.http.get<BankAccount[]>(this.apiUrl);
  }

  getAccount(id: string): Observable<BankAccount> {
    return this.http.get<BankAccount>(`${this.apiUrl}/${id}`);
  }

  getCustomerAccounts(customerId: number): Observable<BankAccount[]> {
    return this.http.get<BankAccount[]>(`${environment.apiUrl}/customers/${customerId}/accounts`);
  }

  getAccountHistory(accountId: string, page: number, size: number): Observable<AccountHistory> {
    return this.http.get<AccountHistory>(
      `${this.apiUrl}/${accountId}/pageOperations?page=${page}&size=${size}`,
    );
  }

  debit(request: DebitRequest): Observable<DebitRequest> {
    return this.http.post<DebitRequest>(`${this.apiUrl}/debit`, request);
  }

  credit(request: CreditRequest): Observable<CreditRequest> {
    return this.http.post<CreditRequest>(`${this.apiUrl}/credit`, request);
  }

  transfer(request: TransferRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/transfer`, request);
  }
}
