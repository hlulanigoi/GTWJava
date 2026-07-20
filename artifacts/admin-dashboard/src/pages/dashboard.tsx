import React from 'react';
import { useGetAdminStats } from '@workspace/api-client-react';
import { getGetAdminStatsQueryKey } from '@workspace/api-client-react';
import { 
  Users, 
  Map, 
  Car, 
  CreditCard, 
  TrendingUp, 
  AlertTriangle,
  ArrowUpRight,
  ArrowRight,
  BarChart3
} from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { Link } from 'wouter';

export default function Dashboard() {
  const { data: stats, isLoading } = useGetAdminStats({
    query: {
      queryKey: getGetAdminStatsQueryKey()
    }
  });

  return (
    <div className="p-8 max-w-[1600px] mx-auto space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight mb-2">Platform Overview</h1>
        <p className="text-muted-foreground">High-level metrics and platform health.</p>
      </div>

      {isLoading || !stats ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {Array.from({ length: 8 }).map((_, i) => (
            <Card key={i} className="shadow-sm">
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <Skeleton className="h-4 w-[100px]" />
                <Skeleton className="h-4 w-4 rounded-full" />
              </CardHeader>
              <CardContent>
                <Skeleton className="h-8 w-[60px] mb-1" />
                <Skeleton className="h-3 w-[120px]" />
              </CardContent>
            </Card>
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {/* Revenue */}
          <Card className="shadow-sm border-l-4 border-l-primary">
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">Total Revenue</CardTitle>
              <TrendingUp className="h-4 w-4 text-primary" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold font-mono">${(stats.totalRevenue / 100).toFixed(2)}</div>
              <p className="text-xs text-muted-foreground mt-1 flex items-center">
                <ArrowUpRight className="h-3 w-3 mr-1 text-emerald-500" />
                <span className="text-emerald-500 font-medium">${(stats.revenueThisWeek / 100).toFixed(2)}</span> this week
              </p>
            </CardContent>
          </Card>

          <Card className="shadow-sm">
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">Revenue This Month</CardTitle>
              <TrendingUp className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold font-mono">${(stats.revenueThisMonth / 100).toFixed(2)}</div>
              <p className="text-xs text-muted-foreground mt-1">Current billing cycle</p>
            </CardContent>
          </Card>

          {/* Action Items */}
          <Card className={`shadow-sm ${stats.pendingPayments > 0 ? 'border-l-4 border-l-destructive bg-destructive/5' : ''}`}>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">Pending Payments</CardTitle>
              <CreditCard className={`h-4 w-4 ${stats.pendingPayments > 0 ? 'text-destructive' : 'text-muted-foreground'}`} />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold font-mono">{stats.pendingPayments}</div>
              <div className="flex items-center justify-between mt-1">
                <p className="text-xs text-muted-foreground">Awaiting verification</p>
                {stats.pendingPayments > 0 && (
                  <Link href="/payments" className="text-xs font-medium text-destructive hover:underline flex items-center">
                    Review <ArrowRight className="h-3 w-3 ml-1" />
                  </Link>
                )}
              </div>
            </CardContent>
          </Card>

          <Card className={`shadow-sm ${stats.pendingDriverApplications && stats.pendingDriverApplications > 0 ? 'border-l-4 border-l-amber-500 bg-amber-500/5' : ''}`}>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">Driver Applications</CardTitle>
              <AlertTriangle className={`h-4 w-4 ${stats.pendingDriverApplications && stats.pendingDriverApplications > 0 ? 'text-amber-500' : 'text-muted-foreground'}`} />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold font-mono">{stats.pendingDriverApplications || 0}</div>
              <div className="flex items-center justify-between mt-1">
                <p className="text-xs text-muted-foreground">Awaiting approval</p>
                {stats.pendingDriverApplications && stats.pendingDriverApplications > 0 ? (
                  <Link href="/drivers" className="text-xs font-medium text-amber-600 hover:underline flex items-center">
                    Review <ArrowRight className="h-3 w-3 ml-1" />
                  </Link>
                ) : null}
              </div>
            </CardContent>
          </Card>

          {/* Volume Stats */}
          <Card className="shadow-sm">
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">Total Users</CardTitle>
              <Users className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold font-mono">{stats.totalUsers.toLocaleString()}</div>
              <p className="text-xs text-muted-foreground mt-1">
                <span className="font-medium text-foreground">{stats.activeUsers.toLocaleString()}</span> active accounts
              </p>
            </CardContent>
          </Card>

          <Card className="shadow-sm">
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">Total Rides</CardTitle>
              <Car className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold font-mono">{stats.totalRides.toLocaleString()}</div>
              <p className="text-xs text-muted-foreground mt-1">All time</p>
            </CardContent>
          </Card>

          <Card className="shadow-sm">
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">Active Trips</CardTitle>
              <Map className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold font-mono">{stats.activeTrips.toLocaleString()}</div>
              <p className="text-xs text-muted-foreground mt-1">In progress or scheduled</p>
            </CardContent>
          </Card>
          
          <Card className="shadow-sm bg-muted/50 border-dashed">
            <CardContent className="flex flex-col items-center justify-center h-full pt-6">
              <BarChart3 className="h-8 w-8 text-muted-foreground mb-2" />
              <Link href="/revenue" className="text-sm font-medium text-primary hover:underline">
                View Full Analytics
              </Link>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}