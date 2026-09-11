import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: 'employee/**',
    renderMode: RenderMode.Client
  },
  {
    path: 'dashboard',
    renderMode: RenderMode.Client
  },
  {
    path: 'kyc',
    renderMode: RenderMode.Client
  },
  {
    path: 'loan-application',
    renderMode: RenderMode.Client
  },
  {
    path: 'request-kyc-edit',
    renderMode: RenderMode.Client
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];
