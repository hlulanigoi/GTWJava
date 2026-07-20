import * as React from "react";
import { Link, useLocation } from "wouter";
import { Package, MapPin, Ticket, User, LogOut, Loader2, Menu } from "lucide-react";
import { useGetMe, useLogout } from "@workspace/api-client-react";
import { setToken } from "../lib/auth";

export function Layout({ children }: { children: React.ReactNode }) {
  const [location, setLocation] = useLocation();
  const { data: user, isLoading } = useGetMe({ query: { retry: false } });
  const logoutMut = useLogout();
  const [mobileMenuOpen, setMobileMenuOpen] = React.useState(false);

  React.useEffect(() => {
    if (!isLoading && !user && location !== "/login" && location !== "/register") {
      setLocation("/login");
    }
  }, [user, isLoading, location, setLocation]);

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  if (!user && (location === "/login" || location === "/register")) {
    return <>{children}</>;
  }

  if (!user) return null;

  const handleLogout = () => {
    logoutMut.mutate(undefined, {
      onSuccess: () => {
        setToken(null);
        setLocation("/login");
      },
    });
  };

  const navItems = [
    { href: "/", label: "Dashboard", icon: MapPin },
    { href: "/parcels", label: "My Parcels", icon: Package },
    { href: "/rides", label: "My Rides", icon: MapPin },
    { href: "/tickets", label: "Tickets", icon: Ticket },
    { href: "/profile", label: "Profile", icon: User },
  ];

  return (
    <div className="min-h-[100dvh] bg-background flex flex-col md:flex-row">
      {/* Mobile Header */}
      <div className="md:hidden flex items-center justify-between p-4 bg-card border-b border-card-border sticky top-0 z-50">
        <div className="font-bold text-lg text-primary flex items-center gap-2">
          <Package className="h-6 w-6" />
          GTW Portal
        </div>
        <button onClick={() => setMobileMenuOpen(!mobileMenuOpen)} className="p-2">
          <Menu className="h-6 w-6 text-foreground" />
        </button>
      </div>

      {/* Sidebar */}
      <div className={`fixed inset-y-0 left-0 transform ${mobileMenuOpen ? "translate-x-0" : "-translate-x-full"} md:translate-x-0 transition-transform duration-200 ease-in-out w-64 bg-card border-r border-card-border z-40 md:relative flex flex-col`}>
        <div className="hidden md:flex items-center gap-2 p-6 font-bold text-xl text-primary border-b border-card-border">
          <Package className="h-6 w-6" />
          GTW Portal
        </div>

        <nav className="flex-1 px-4 py-6 space-y-2 overflow-y-auto">
          {navItems.map((item) => {
            const isActive = location === item.href || (item.href !== "/" && location.startsWith(item.href));
            return (
              <Link key={item.href} href={item.href} onClick={() => setMobileMenuOpen(false)}>
                <span className={`flex items-center gap-3 px-4 py-3 rounded-lg font-medium transition-colors ${
                  isActive ? "bg-primary text-primary-foreground" : "text-muted-foreground hover:bg-secondary hover:text-foreground"
                }`}>
                  <item.icon className="h-5 w-5" />
                  {item.label}
                </span>
              </Link>
            );
          })}
        </nav>

        <div className="p-4 border-t border-card-border">
          <button 
            onClick={handleLogout}
            className="flex items-center gap-3 px-4 py-3 w-full text-left rounded-lg font-medium text-destructive hover:bg-destructive/10 transition-colors"
          >
            <LogOut className="h-5 w-5" />
            Sign Out
          </button>
        </div>
      </div>

      {/* Main Content */}
      <main className="flex-1 overflow-y-auto w-full">
        {mobileMenuOpen && (
          <div 
            className="fixed inset-0 bg-black/20 z-30 md:hidden"
            onClick={() => setMobileMenuOpen(false)}
          />
        )}
        {children}
      </main>
    </div>
  );
}
