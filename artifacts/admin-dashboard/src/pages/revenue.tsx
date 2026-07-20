import React, { useState } from 'react';
import { 
  useGetRevenueReport, 
  getGetRevenueReportQueryKey 
} from '@workspace/api-client-react';
import { 
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, AreaChart, Area
} from 'recharts';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { 
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue 
} from '@/components/ui/select';
import { Skeleton } from '@/components/ui/skeleton';
import { DollarSign, TrendingUp } from 'lucide-react';

export default function RevenuePage() {
  const [period, setPeriod] = useState<'week' | 'month' | 'year'>('month');

  const { data: report, isLoading } = useGetRevenueReport(
    { period },
    { 
      query: { 
        queryKey: getGetRevenueReportQueryKey({ period }) 
      } 
    }
  );

  // Transform data for recharts
  const chartData = report?.buckets.map(b => ({
    name: b.label,
    total: b.amount / 100 // Convert cents to dollars
  })) || [];

  return (
    <div className="p-8 max-w-[1600px] mx-auto space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Revenue Analytics</h1>
          <p className="text-muted-foreground">Financial performance and transaction volume.</p>
        </div>
        
        <Select 
          value={period} 
          onValueChange={(val: any) => setPeriod(val)}
        >
          <SelectTrigger className="w-[180px] bg-card">
            <SelectValue placeholder="Select Period" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="week">Past 7 Days</SelectItem>
            <SelectItem value="month">This Month</SelectItem>
            <SelectItem value="year">This Year</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card className="md:col-span-1 shadow-sm border-l-4 border-l-primary bg-primary/5">
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground uppercase tracking-wider">Total Revenue ({period})</CardTitle>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <Skeleton className="h-10 w-[120px]" />
            ) : (
              <div className="text-4xl font-bold font-mono">
                ${((report?.total || 0) / 100).toFixed(2)}
              </div>
            )}
            <p className="text-sm text-muted-foreground mt-2 flex items-center">
              <TrendingUp className="w-4 h-4 mr-1 text-primary" /> 
              Platform fees collected
            </p>
          </CardContent>
        </Card>
      </div>

      <Card className="shadow-sm">
        <CardHeader>
          <CardTitle>Revenue Trend</CardTitle>
          <CardDescription>Earnings over the selected period</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="h-[400px] w-full mt-4">
            {isLoading ? (
              <div className="h-full w-full flex items-end gap-2 pb-8">
                {Array.from({ length: 12 }).map((_, i) => (
                  <Skeleton key={i} className="w-full" style={{ height: `${Math.max(10, Math.random() * 80)}%` }} />
                ))}
              </div>
            ) : chartData.length === 0 ? (
              <div className="h-full w-full flex items-center justify-center text-muted-foreground">
                No revenue data available for this period.
              </div>
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="hsl(var(--border))" />
                  <XAxis 
                    dataKey="name" 
                    axisLine={false}
                    tickLine={false}
                    tick={{ fill: 'hsl(var(--muted-foreground))', fontSize: 12 }}
                    dy={10}
                  />
                  <YAxis 
                    axisLine={false}
                    tickLine={false}
                    tick={{ fill: 'hsl(var(--muted-foreground))', fontSize: 12 }}
                    tickFormatter={(value) => `$${value}`}
                  />
                  <Tooltip 
                    cursor={{ fill: 'hsl(var(--muted))' }}
                    contentStyle={{ 
                      backgroundColor: 'hsl(var(--card))', 
                      borderColor: 'hsl(var(--border))',
                      borderRadius: '8px',
                      boxShadow: 'var(--shadow-sm)'
                    }}
                    itemStyle={{ color: 'hsl(var(--foreground))', fontWeight: 500 }}
                    formatter={(value: number) => [`$${value.toFixed(2)}`, 'Revenue']}
                  />
                  <Bar 
                    dataKey="total" 
                    fill="hsl(var(--primary))" 
                    radius={[4, 4, 0, 0]}
                    maxBarSize={50}
                  />
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>
        </CardContent>
      </Card>
      
      <Card className="shadow-sm">
        <CardHeader>
          <CardTitle>Cumulative Growth</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="h-[300px] w-full">
            {isLoading ? (
               <Skeleton className="h-full w-full" />
            ) : chartData.length === 0 ? (
              <div className="h-full w-full flex items-center justify-center text-muted-foreground">
                No data available.
              </div>
            ) : (() => {
              // Calculate cumulative data
              let sum = 0;
              const cumulativeData = chartData.map(d => {
                sum += d.total;
                return { name: d.name, cumulative: sum };
              });
              
              return (
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={cumulativeData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                    <defs>
                      <linearGradient id="colorCumulative" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="hsl(var(--primary))" stopOpacity={0.3}/>
                        <stop offset="95%" stopColor="hsl(var(--primary))" stopOpacity={0}/>
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="hsl(var(--border))" />
                    <XAxis 
                      dataKey="name" 
                      axisLine={false}
                      tickLine={false}
                      tick={{ fill: 'hsl(var(--muted-foreground))', fontSize: 12 }}
                      dy={10}
                    />
                    <YAxis 
                      axisLine={false}
                      tickLine={false}
                      tick={{ fill: 'hsl(var(--muted-foreground))', fontSize: 12 }}
                      tickFormatter={(value) => `$${value}`}
                    />
                    <Tooltip 
                      contentStyle={{ 
                        backgroundColor: 'hsl(var(--card))', 
                        borderColor: 'hsl(var(--border))',
                        borderRadius: '8px'
                      }}
                      formatter={(value: number) => [`$${value.toFixed(2)}`, 'Total']}
                    />
                    <Area 
                      type="monotone" 
                      dataKey="cumulative" 
                      stroke="hsl(var(--primary))" 
                      strokeWidth={2}
                      fillOpacity={1} 
                      fill="url(#colorCumulative)" 
                    />
                  </AreaChart>
                </ResponsiveContainer>
              );
            })()}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}