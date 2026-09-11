import { Component, OnInit, ChangeDetectorRef } from '@angular/core'; 
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { UserService, LoanDetailsResponse, Loan } from '../../core/services/user/user.service'; 
import { HttpErrorResponse } from '@angular/common/http'; 
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-loan-details',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './loan-details.html',
  styleUrls: ['./loan-details.css']
})
export class LoanDetails implements OnInit {
  loanId: string | null = null;
  public userService: UserService; 
  loanDetails: LoanDetailsResponse | null = null; 
  loadingInitialData: boolean = true; 
  errorMessage: string | null = null; 

  // Modal State: Evaluation
  showEvaluationModal: boolean = false;
  evaluationQualityIndex: number = 95;
  evaluationOfferAmount: number = 0;
  isSubmittingEvaluation: boolean = false;

  // Modal State: Rejection
  showRejectionModal: boolean = false;
  rejectionReasonInput: string = '';
  rejectionActionType: 'REVIEW' | 'PERMANENT' = 'REVIEW';
  isSubmittingRejection: boolean = false;

  constructor(
    private route: ActivatedRoute,
    userService: UserService, 
    private router: Router,
    private cdr: ChangeDetectorRef,
    private snackBar: MatSnackBar
  ) {
    this.userService = userService; 
  }

  ngOnInit(): void {
    this.loanId = this.route.snapshot.paramMap.get('id');
    if (this.loanId) {
      this.fetchLoanDetails(this.loanId);
    } else {
      this.router.navigate(['/employee/dashboard']);
    }
  }

  fetchLoanDetails(rid: string): void {
    this.loadingInitialData = true; 
    this.errorMessage = null;      

    this.userService.getLoanDetailsById(rid).subscribe({
      next: (data: LoanDetailsResponse) => {
        this.loanDetails = data;
        this.loadingInitialData = false;
        this.cdr.detectChanges(); 
      },
      error: (err: HttpErrorResponse) => { 
        console.error('Failed to fetch loan details:', err);
        this.loadingInitialData = false; 
        this.loanDetails = null; 

        this.errorMessage = err.status === 404 
                          ? 'Loan Application not found.'
                          : 'Failed to load details. Please check server logs.';
        this.cdr.detectChanges(); 
      }
    });
  }
  
  private notify(message: string, isError: boolean = false): void {
    this.snackBar.open(message, 'Close', {
      duration: 4000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: isError ? ['bg-red-600', 'text-white'] : ['bg-green-600', 'text-white']
    });
  }

  // --- Step 3 Gold Inspection Actions ---
  handleGoldReceipt(isAccept: boolean): void {
    if (!this.loanId || this.loanDetails?.status.toUpperCase() !== 'GOLD_SUBMITTED') return;

    if (isAccept) {
      this.openEvaluationModal();
    } else {
      this.openRejectionModal('PERMANENT');
    }
  }

  // --- Evaluation Modal Handlers ---
  openEvaluationModal(): void {
    const requestedAmount = this.loanDetails?.amount || 0;
    this.evaluationOfferAmount = Math.round(requestedAmount * 0.90);
    this.evaluationQualityIndex = 95;
    this.showEvaluationModal = true;
    this.cdr.detectChanges();
  }

  closeEvaluationModal(): void {
    this.showEvaluationModal = false;
    this.isSubmittingEvaluation = false;
  }

  submitEvaluation(): void {
    if (!this.loanId || this.evaluationOfferAmount <= 0 || this.evaluationQualityIndex <= 0) {
      this.errorMessage = 'Please enter valid positive values for Quality Index and Offer Amount.';
      return;
    }

    this.isSubmittingEvaluation = true;
    this.userService.submitEvaluationData(this.loanId, this.evaluationOfferAmount, this.evaluationQualityIndex).subscribe({
      next: () => {
        this.isSubmittingEvaluation = false;
        this.closeEvaluationModal();
        this.errorMessage = null;
        this.notify('Gold evaluated successfully! Status updated to EVALUATED.');
        this.fetchLoanDetails(this.loanId!);
      },
      error: (err: any) => {
        this.isSubmittingEvaluation = false;
        console.error('Evaluation failed:', err);
        this.errorMessage = err.error?.message || 'Failed to complete evaluation. Check inputs/server.';
        this.notify(this.errorMessage || 'Evaluation failed', true);
      }
    });
  }

  // --- Rejection Modal Handlers ---
  openRejectionModal(type: 'REVIEW' | 'PERMANENT'): void {
    this.rejectionActionType = type;
    this.rejectionReasonInput = '';
    this.showRejectionModal = true;
    this.cdr.detectChanges();
  }

  closeRejectionModal(): void {
    this.showRejectionModal = false;
    this.isSubmittingRejection = false;
  }

  confirmRejection(): void {
    if (!this.loanId) return;

    if (this.rejectionActionType === 'REVIEW' && (!this.rejectionReasonInput || !this.rejectionReasonInput.trim())) {
      this.errorMessage = 'A rejection reason is required to send back for review.';
      return;
    }

    this.isSubmittingRejection = true;
    const newStatus: Loan['status'] = this.rejectionActionType === 'REVIEW' ? 'REJECTED_FOR_REVIEW' : 'REJECTED';
    const reason = this.rejectionReasonInput.trim() || undefined;

    this.userService.updateLoanStatus(this.loanId, newStatus, reason).subscribe({
      next: () => {
        this.isSubmittingRejection = false;
        this.closeRejectionModal();
        const msg = this.rejectionActionType === 'REVIEW' 
          ? 'Loan sent back for review.' 
          : 'Loan permanently rejected.';
        this.notify(msg);
        this.fetchLoanDetails(this.loanId!);
      },
      error: (err: any) => {
        this.isSubmittingRejection = false;
        console.error('Rejection failed:', err);
        this.errorMessage = 'Action failed. Please try again.';
        this.notify('Action failed. Please try again.', true);
      }
    });
  }

  // --- Step 1 Action: Verify Details ---
  verifyDetails(isCorrect: boolean): void {
    if (!this.loanId || !this.loanDetails) return;

    if (isCorrect) {
      this.userService.updateLoanStatus(this.loanId, 'VERIFIED').subscribe({
        next: () => {
          this.notify('Details verified successfully. Status updated to VERIFIED.');
          this.fetchLoanDetails(this.loanId!);
        },
        error: (err: any) => {
          console.error('Verification failed', err);
          this.errorMessage = 'Verification failed. Please try again.';
          this.notify('Verification failed. Please try again.', true);
        }
      });
    } else {
      this.openRejectionModal('REVIEW');
    }
  }

  // --- Step 4 Action: Send Offer ---
  sendOffer(offerStatus: 'Offer Made' | 'REJECTED'): void {
    if (!this.loanId || this.loanDetails?.status.toUpperCase() !== 'EVALUATED') return;

    const newStatus: Loan['status'] = offerStatus === 'Offer Made' ? 'OFFER_MADE' : 'REJECTED';

    this.userService.updateLoanStatus(this.loanId, newStatus).subscribe({
      next: () => {
        const message = newStatus === 'OFFER_MADE' 
          ? 'Loan offer sent to customer. Status updated to OFFER_MADE.' 
          : 'Loan rejected. Status updated to REJECTED.';
        this.notify(message);
        this.fetchLoanDetails(this.loanId!);
      },
      error: (err: any) => {
        console.error('Offer action failed', err);
        this.errorMessage = 'Offer action failed. Please try again.';
        this.notify('Offer action failed. Please try again.', true);
      }
    });
  }

  approveLoan(): void {
    this.sendOffer('Offer Made');
  }

  rejectLoan(): void {
    this.sendOffer('REJECTED');
  }

  // --- Step 6 Action: Disburse Loan ---
  disburseLoan(): void {
    if (!this.loanId || this.loanDetails?.status.toUpperCase() !== 'OFFER_ACCEPTED') return;

    this.userService.disburseLoan(this.loanId).subscribe({
      next: () => {
        this.notify('Loan disbursed successfully! Status updated to DISBURSED.');
        this.fetchLoanDetails(this.loanId!);
      },
      error: (err: any) => {
        console.error('Disbursement failed', err);
        this.errorMessage = 'Disbursement failed. Please try again.';
        this.notify('Disbursement failed. Please try again.', true);
      }
    });
  }

  handleCollectGold(rid: string): void {
    if (this.loanDetails?.status.toUpperCase() !== 'PAID_FINE') return;

    this.userService.collectGold(rid).subscribe({
      next: () => {
        this.notify('Gold returned to customer. Loan marked as GOLD_COLLECTED.');
        this.fetchLoanDetails(rid);
      },
      error: (err: any) => {
        console.error('Collection failed', err);
        this.errorMessage = 'Failed to record gold collection.';
        this.notify('Failed to record gold collection.', true);
      }
    });
  }
}