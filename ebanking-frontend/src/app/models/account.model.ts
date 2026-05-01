export interface AccountOperation {
  id: number;
  operationDate: Date;
  amount: number;
  type: 'DEBIT' | 'CREDIT';
  description: string;
}

export interface AccountHistory {
  accountId: string;
  balance: number;
  currentPage: number;
  totalPages: number;
  pageSize: number;
  accountOperationDTOS: AccountOperation[];
}

export interface BankAccount {
  id: string;
  balance: number;
  createdAt: Date;
  status: 'CREATED' | 'ACTIVATED' | 'SUSPENDED';
  customerDTO: {
    id: number;
    name: string;
    email: string;
  };
  type: string;
  overDraft?: number;
  interestRate?: number;
}

export interface DebitRequest {
  accountId: string;
  amount: number;
  description: string;
}

export interface CreditRequest {
  accountId: string;
  amount: number;
  description: string;
}

export interface TransferRequest {
  accountSource: string;
  accountDestination: string;
  amount: number;
  description: string;
}
