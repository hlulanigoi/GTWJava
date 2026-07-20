import React, { useEffect } from 'react';
import { useLocation, Link } from 'wouter';
import { useGetMe } from '@workspace/api-client-react';
import { getGetMeQueryKey } from '@workspace/api-client-react';
import { getAuthToken } from '@/lib/auth';
import { 
  LayoutDashboard, 
  Users, 
  Car, 
  Map, 
  Ticket, 
  CreditCard, 
  BarChart3, 
  LogOut,
  ShieldAlert
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { setAuthToken } from '@/lib/auth';
import { useQueryClient } from '@tanstack/react-query';
import { Spinner } from '@/components/ui/spinner';

export function Shell({ children }: { children: React.ReactNode }) {
  const [location, setLocation] = useLocation();
  const token = getAuthToken();
  const queryClient = useQueryClient();

  const { data: user, isLoading, isError } = useGetMe({ 
    query: { 
      enabled: !!token, 
      queryKey: getGetMeQueryKey() 
    } 
  });

  useEffect(() => {
    if (!token || isError) {
      setLocation('/login');
    }
  }, [token, isError, setLocation]);

  if (!token) return null;

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <Spinner className="size-8" />
      </div>
    );
  }

  if (user && user.role !== 'ADMIN') {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-background p-4 text-center">
        <ShieldAlert className="h-12 w-12 text-destructive mb-4" />
        <h1 className="text-2xl font-bold mb-2">Access Denied</h1>
        <p className="text-muted-foreground mb-6">You must be an admin to access this dashboard.</p>
        <Button onClick={() => {
          setAuthToken(null);
          setLocation('/login');
        }}>Sign Out</Button>
      </div>
    );
  }

  const handleLogout = () => {
    setAuthToken(null);
    queryClient.clear();
    setLocation('/login');
  };

  const navItems = [
    { href: '/', label: 'Dashboard', icon: LayoutDashboard },
    { href: '/users', label: 'Users', icon: Users },
    { href: '/drivers', label: 'Drivers', icon: Car },
    { href: '/rides', label: 'Rides', icon: Map },
    { href: '/trips', label: 'Trips', icon: Map },
    { href: '/tickets', label: 'Tickets', icon: Ticket },
    { href: '/payments', label: 'Payments', icon: CreditCard },
    { href: '/revenue', label: 'Revenue', icon: BarChart3 },
  ];

  return (
    <div className="flex min-h-screen bg-background text-foreground font-sans">
      {/* Sidebar */}
      <aside className="w-64 bg-sidebar text-sidebar-foreground flex flex-col border-r border-sidebar-border shrink-0">
        <div className="h-16 flex items-center px-6 border-b border-sidebar-border shrink-0">
          <div className="flex items-center gap-2 font-bold text-lg tracking-tight">
            <div className="w-6 h-6 bg-primary rounded flex items-center justify-center text-primary-foreground">
              <Map className="w-4 h-4" />
            </div>
            <span>Going That Way</span>
          </div>
        </div>
        
        <nav className="flex-1 py-6 px-3 flex flex-col gap-1 overflow-y-auto">
          <div className="px-3 text-xs font-semibold text-sidebar-foreground/50 uppercase tracking-wider mb-2">
            Overview
          </div>
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = location === item.href || (item.href !== '/' && location.startsWith(item.href));
            return (
              <Link key={item.href} href={item.href} className={`flex items-center gap-3 px-3 py-2 rounded-md text-sm transition-colors ${
                isActive 
                  ? 'bg-sidebar-accent text-sidebar-accent-foreground font-medium' 
                  : 'text-sidebar-foreground/70 hover:bg-sidebar-accent/50 hover:text-sidebar-foreground'
              }`}>
                <Icon className="w-4 h-4" />
                {item.label}
              </Link>
            );
          })}
        </nav>

        <div className="p-4 border-t border-sidebar-border shrink-0">
          <div className="flex items-center gap-3 px-3 py-2 mb-2">
            <div className="w-8 h-8 rounded-full bg-sidebar-accent flex items-center justify-center text-sm font-medium">
              {user?.fullName?.charAt(0) || 'A'}
            </div>
            <div className="flex flex-col">
              <span className="text-sm font-medium leading-none">{user?.fullName}</span>
              <span className="text-xs text-sidebar-foreground/50">{user?.email}</span>
            </div>
          </div>
          <Button 
            variant="ghost" 
            className="w-full justify-start text-sidebar-foreground/70 hover:text-sidebar-foreground hover:bg-sidebar-accent/50" 
            onClick={handleLogout}
          >
            <LogOut className="w-4 h-4 mr-2" />
            Sign Out
          </Button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 flex flex-col min-w-0 h-screen overflow-y-auto bg-muted/30">
        {children}
      </main>
    </div>
  );
}