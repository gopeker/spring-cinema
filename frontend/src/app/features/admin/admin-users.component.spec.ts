import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { AdminUsersComponent } from './admin-users.component';
import { Page, UserDto } from '../../core/models/api.models';

const mockUserPage: Page<UserDto> = {
  content: [
    { id: 1, name: 'John Doe', email: 'john@example.com', role: 'USER', address: { city: 'Springfield' } },
    { id: 2, name: 'Jane Admin', email: 'jane@example.com', role: 'ADMIN', address: { city: 'Portland' } },
  ],
  totalElements: 2, totalPages: 1, size: 10, number: 0,
};

describe('AdminUsersComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminUsersComponent, NoopAnimationsModule],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => TestBed.resetTestingModule());

  it('should create and load users on init', () => {
    const fixture = TestBed.createComponent(AdminUsersComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/users').flush(mockUserPage);

    const c = fixture.componentInstance;
    expect(c.users()).toHaveLength(2);
    expect(c.totalElements()).toBe(2);
    expect(c.loading()).toBe(false);
  });

  it('should load with default paging/sort params', () => {
    const fixture = TestBed.createComponent(AdminUsersComponent);
    fixture.detectChanges();
    const req = httpMock.expectOne(r => r.url === '/api/admin/users');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('10');
    expect(req.request.params.get('sort')).toBe('id,asc');
    req.flush(mockUserPage);
  });

  it('doSearch should set hasSearch and call search endpoint', () => {
    const fixture = TestBed.createComponent(AdminUsersComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/users').flush(mockUserPage);

    fixture.componentInstance.updateSearch({ name: 'John' });
    fixture.componentInstance.doSearch();

    expect(fixture.componentInstance.hasSearch()).toBe(true);
    expect(fixture.componentInstance.pageIndex()).toBe(0);

    const req = httpMock.expectOne(r => r.url === '/api/admin/users/search');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ name: 'John' });
    req.flush(mockUserPage);
  });

  it('clearSearch should reset and call getUsers', () => {
    const fixture = TestBed.createComponent(AdminUsersComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/users').flush(mockUserPage);

    fixture.componentInstance.doSearch();
    httpMock.expectOne(r => r.url === '/api/admin/users/search').flush(mockUserPage);

    fixture.componentInstance.clearSearch();

    expect(fixture.componentInstance.hasSearch()).toBe(false);
    expect(fixture.componentInstance.search()).toEqual({});

    const req = httpMock.expectOne(r => r.url === '/api/admin/users');
    expect(req.request.method).toBe('GET');
    req.flush(mockUserPage);
  });

  it('updateSearch should merge fields', () => {
    const fixture = TestBed.createComponent(AdminUsersComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/users').flush(mockUserPage);

    const c = fixture.componentInstance;
    c.updateSearch({ name: 'John' });
    expect(c.search().name).toBe('John');

    c.updateSearch({ city: 'Springfield' });
    expect(c.search().name).toBe('John');
    expect(c.search().city).toBe('Springfield');
  });

  it('onSearchKeydown should trigger doSearch on Enter', () => {
    const fixture = TestBed.createComponent(AdminUsersComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/users').flush(mockUserPage);

    fixture.componentInstance.onSearchKeydown(new KeyboardEvent('keydown', { key: 'Enter' }));

    expect(fixture.componentInstance.hasSearch()).toBe(true);
    httpMock.expectOne(r => r.url === '/api/admin/users/search').flush(mockUserPage);
  });

  it('onSearchKeydown should ignore non-Enter keys', () => {
    const fixture = TestBed.createComponent(AdminUsersComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/users').flush(mockUserPage);

    fixture.componentInstance.onSearchKeydown(new KeyboardEvent('keydown', { key: 'Tab' }));

    expect(fixture.componentInstance.hasSearch()).toBe(false);
  });

  it('onSortChange should toggle direction correctly', () => {
    const fixture = TestBed.createComponent(AdminUsersComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/users').flush(mockUserPage);

    fixture.componentInstance.onSortChange({ active: 'name', direction: 'desc' });
    expect(fixture.componentInstance.sortDirection()).toBe('desc');
    httpMock.expectOne(r => r.url === '/api/admin/users').flush(mockUserPage);

    fixture.componentInstance.onSortChange({ active: 'name', direction: '' as any });
    expect(fixture.componentInstance.sortDirection()).toBe('asc');
    httpMock.expectOne(r => r.url === '/api/admin/users').flush(mockUserPage);
  });

  it('onPageChange should update paging and reload', () => {
    const fixture = TestBed.createComponent(AdminUsersComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/users').flush(mockUserPage);

    fixture.componentInstance.onPageChange({ pageIndex: 1, pageSize: 25, length: 100 });

    expect(fixture.componentInstance.pageIndex()).toBe(1);
    expect(fixture.componentInstance.pageSize()).toBe(25);

    const req = httpMock.expectOne(r => r.url === '/api/admin/users');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('size')).toBe('25');
    req.flush(mockUserPage);
  });

  it('deleteUser should call API when confirmed', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    const fixture = TestBed.createComponent(AdminUsersComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/users').flush(mockUserPage);

    fixture.componentInstance.deleteUser(1);

    const req = httpMock.expectOne('/api/admin/users/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);

    httpMock.expectOne(r => r.url === '/api/admin/users').flush(mockUserPage);
  });

  it('deleteUser should not call API when cancelled', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(false);
    const fixture = TestBed.createComponent(AdminUsersComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/users').flush(mockUserPage);

    fixture.componentInstance.deleteUser(1);

    httpMock.expectNone(r => r.url === '/api/admin/users/1' && r.method === 'DELETE');
  });

  it('should handle error gracefully', () => {
    const fixture = TestBed.createComponent(AdminUsersComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/users').flush('Error', { status: 500, statusText: 'Server Error' });

    expect(fixture.componentInstance.loading()).toBe(false);
    expect(fixture.componentInstance.users()).toHaveLength(0);
  });

  it('search with paging should send correct params', () => {
    const fixture = TestBed.createComponent(AdminUsersComponent);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/api/admin/users').flush(mockUserPage);

    fixture.componentInstance.updateSearch({ city: 'Portland' });
    fixture.componentInstance.doSearch();

    const firstReq = httpMock.expectOne(r => r.url === '/api/admin/users/search');
    firstReq.flush(mockUserPage);

    fixture.componentInstance.onPageChange({ pageIndex: 1, pageSize: 5, length: 20 });

    const req = httpMock.expectOne(r => r.url === '/api/admin/users/search');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('size')).toBe('5');
    expect(req.request.body).toEqual({ city: 'Portland' });
    req.flush(mockUserPage);
  });
});
