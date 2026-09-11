// src/app/kyc/kyc-form/kyc-form.ts

import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../core/services/auth.service'; 
import { UserService } from '../../core/services/user/user.service'; // 💡 FIX 1: Import UserService
import { isPlatformBrowser } from '@angular/common';
import { catchError, Observable, of, switchMap, tap } from 'rxjs'; // NEW: RxJS operators for cache fallback and error handling
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

// --- INTERFACES (Must be available, typically imported from user.service.ts or defined globally) ---
// Note: These interfaces are typically defined in user.service.ts; they are included here for compilation context.
interface CustomerDetailsResponse {
  id: number;
  fullName: string;
  knNumber: string;
  gender: string;
  mobileNumber: string;
  emailId: string;
  kycVerified: boolean;
  bankName: string;
  accountHolderName: string;
  accountNumber: string;
  ifscCode: string;
  branchName: string;
}

interface LoanApplicationPayload {
    customerId: number | null;
    amountSeeking: number;
    itemType: string;
    numberOfItems: number;
    purity: string;
    netWeight: number;
    photos: File | null;
    acknowledgement: boolean;
}

interface LoanSubmitResponse {
    message: string;
    rid: string;
}

interface CustomerProfile {
  name: string;
  email: string;
  mobileNumber: string;
  kycStatus: boolean;
  knNumber: string | null;
  aadhaar: string | null;
  panCard: string | null;
  dateOfBirth: string | null;
  gender: string | null;
  fullAddress: string | null;
  city: string | null;
  state: string | null;
  pinCode: string | null;
  occupation: string | null;
  income: string | null;
  bankAccountNumber: string | null;
  ifscCode: string | null;
  existingLoans: string | null;
}
// ----------------------------------------------------------------------------------

@Component({
  selector: 'app-kyc-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatSnackBarModule],
  templateUrl: './kyc-form.html',
  styleUrls: ['./kyc-form.css'] 
})
export class KycFormComponent implements OnInit {
  kycForm!: FormGroup; // 💡 FIX 2: Correctly declared, holds the form controls
  
  customerProfile: CustomerProfile | null = null;
  isKycVerified: boolean = false; // Correct property name
  
  submitting = false;
  successMessage: string | null = null;
  errorMessage: string | null = null;

  // 💡 FIX 3: Declaring missing properties
  customerId: number | null = null; 
  userService: UserService; // Declaring UserService for scope access

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
    userService: UserService, // 💡 FIX 4: Injecting UserService in constructor
    private snackBar: MatSnackBar,
    @Inject(PLATFORM_ID) private platformId: Object // NEW: Inject PLATFORM_ID for SSR checks
  ) {
    this.userService = userService; // Assigning injected UserService
  }

  private notify(message: string, isError: boolean = false): void {
    this.snackBar.open(message, 'Close', {
      duration: 4000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: isError ? ['bg-red-600', 'text-white'] : ['bg-green-600', 'text-white']
    });
  }

  ngOnInit(): void {
    // 1. Initialize the form structure
    this.kycForm = this.fb.group({
      fullName: ['', Validators.required],
      dateOfBirth: ['', Validators.required],
      gender: ['', Validators.required],
      mobileNumber: ['', [Validators.required, Validators.pattern("^[0-9]{10}$")]],
      email: ['', [Validators.required, Validators.email]],
      aadhaarNumber: ['', [Validators.required, Validators.pattern("^[0-9]{12}$")]],
      panNumber: ['', [Validators.required, Validators.pattern("^[A-Z]{5}[0-9]{4}[A-Z]{1}$")]],
      passportNumber: [''],
      address: ['', Validators.required],
      city: ['', Validators.required],
      state: ['', Validators.required],
      pinCode: ['', [Validators.required, Validators.pattern("^[0-9]{6}$")]],
      occupation: ['', Validators.required],
      income: ['', Validators.required],
      bankAccountNumber: ['', [Validators.required, Validators.pattern("^[0-9]*$")]],
      ifscCode: ['', [Validators.required, Validators.pattern("^[A-Z]{4}0[A-Z0-9]{6}$")]],
      existingLoans: ['', Validators.required],
      declaration: [false, Validators.requiredTrue]
    });
    
    // NEW: SSR Safety: Skip initialization during server rendering
    if (!isPlatformBrowser(this.platformId)) {
      console.debug('KycFormComponent: Skipping initialization in SSR context.');
      return;
    }

    // 2. Safely load profile with cache fallback (replaces direct call to loadCustomerProfile)
    this.loadCustomerProfileSafely();
    // Note: The old kycForm.valueChanges logic was removed as it was related to loan calc.
  }
  
  // NEW: Enhanced method with SSR check, cache fallback, and error handling
  private loadCustomerProfileSafely(): void {
    // NEW: SSR Safety: Skip API calls during server rendering
    if (!isPlatformBrowser(this.platformId)) {
      console.debug('KycFormComponent: Skipping profile load in SSR.');
      return;
    }

    // NEW: Prioritize cached profile for immediate pre-fill
    this.authService.currentProfile$.pipe(
      switchMap(cachedProfile => {
        if (cachedProfile) {
          console.log('loadCustomerProfileSafely: Cache hit; pre-filling from cache.');
          return of(cachedProfile);
        }
        console.log('loadCustomerProfileSafely: Cache miss; fetching from API.');
        return this.authService.getCustomerProfile(); // Relies on getToken() or interceptor
      }),
      tap(profile => {
        this.processProfile(profile);
      }),
      catchError(err => {
        console.warn('loadCustomerProfileSafely: Profile fetch failed for pre-fill purposes. Error:', err);
        this.errorMessage = 'Failed to load profile data. Please enter details manually.';
        // NEW: Do not throw; allow form to render with empty fields
        return of(null as any);
      })
    ).subscribe(() => {
      // Ensure form is enabled by default if no profile
      if (!this.customerProfile) {
        this.kycForm.enable();
      }
    });
  }

  // UPDATED: Extracted logic from original loadCustomerProfile for reusability
  private processProfile(profile: any): void {
    this.customerProfile = profile;
    this.isKycVerified = profile.kycStatus || false; 
    
    // Patch all received fields into the form
    this.kycForm.patchValue({
        fullName: profile.name,
        email: profile.email,
        mobileNumber: profile.mobileNumber,
        aadhaarNumber: profile.aadhaar, 
        panNumber: profile.panCard,
        dateOfBirth: profile.dateOfBirth,
        gender: profile.gender,
        address: profile.fullAddress,
        city: profile.city,
        state: profile.state,
        pinCode: profile.pinCode,
        occupation: profile.occupation,
        income: profile.income,
        bankAccountNumber: profile.bankAccountNumber,
        ifscCode: profile.ifscCode,
        existingLoans: profile.existingLoans,
        // declaration should be false/unchecked by default even if verified
    });
    
    // If KYC is already verified, disable core identity fields
    if (this.isKycVerified) {
        this.kycForm.get('fullName')?.disable();
        this.kycForm.get('dateOfBirth')?.disable();
        this.kycForm.get('aadhaarNumber')?.disable();
        this.kycForm.get('panNumber')?.disable();
        this.kycForm.get('email')?.disable();
        this.kycForm.get('declaration')?.disable(); // Lock the declaration
    } else {
         this.kycForm.enable();
    }

    // NEW: Update cache if fetched fresh
    if (profile) {
      this.authService.setCurrentProfile(profile);
    }
  }

  onSubmit(): void {
    this.submitting = true;

    if (!this.isKycVerified) { 
        // --- 1. Initial KYC Submission ---
        if (this.kycForm.valid && this.kycForm.get('declaration')?.value) {
            this.notify('Application for KYC submitted. Processing verification...');
            const formData = this.kycForm.value;

            this.authService.submitKyc(formData).subscribe({
                next: (response: any) => {
                    this.notify(`KYC verified successfully! Your KN Number is: ${response.knNumber}. Redirecting...`);
                    this.submitting = false;
                    this.loadCustomerProfileSafely(); // NEW: Use safe reload
                    
                    setTimeout(() => {
                        this.router.navigate(['/dashboard']);
                    }, 2500); 
                },
                error: (err: HttpErrorResponse) => {
                    this.notify(`KYC verification failed: ${err.error?.message || 'Server error'}`, true);
                    this.errorMessage = null; // Clear success message from display
                    this.submitting = false;
                }
              });
        } else {
            this.kycForm.markAllAsTouched();
            this.notify('Please complete all required fields and agree to the declaration.', true);
            this.submitting = false;
        }
    } else {
        // --- 2. Post-KYC Profile Update (Self-Service) ---
        // Check only the enabled, mutable fields for validity
        if (this.kycForm.get('mobileNumber')?.valid && 
            this.kycForm.get('address')?.valid &&
            this.kycForm.get('city')?.valid &&
            this.kycForm.get('state')?.valid &&
            this.kycForm.get('pinCode')?.valid &&
            this.kycForm.get('occupation')?.valid &&
            this.kycForm.get('income')?.valid &&
            this.kycForm.get('bankAccountNumber')?.valid &&
            this.kycForm.get('ifscCode')?.valid &&
            this.kycForm.get('existingLoans')?.valid
        ) {
            this.notify('Submitting updated profile details...');
            // Use getRawValue() to include disabled (unchanged) core fields
            const formData = this.kycForm.getRawValue(); 

            this.authService.submitKyc(formData).subscribe({
                next: (response: any) => {
                    this.notify('Profile updated successfully!');
                    this.submitting = false;
                    this.loadCustomerProfileSafely(); // NEW: Use safe reload
                },
                error: (err: HttpErrorResponse) => {
                    this.notify(`Profile update failed: ${err.error?.message || 'Server error'}`, true);
                    this.submitting = false;
                }
            });
        } else {
            this.kycForm.markAllAsTouched();
            this.notify('Please ensure all editable fields are valid.', true);
            this.submitting = false;
        }
    }
  }

  get f() { return this.kycForm.controls; }
}