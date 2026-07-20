import React, { useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { 
  useListAdminTickets, 
  getListAdminTicketsQueryKey,
  useUpdateTicketPrice
} from '@workspace/api-client-react';
import { 
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow 
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { 
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue 
} from '@/components/ui/select';
import { format } from 'date-fns';
import { Ticket as TicketIcon, Edit2, Check, X } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';

const getStatusBadge = (status: string) => {
  switch(status) {
    case 'ACTIVE': return <Badge variant="outline" className="bg-emerald-50 text-emerald-600 border-emerald-200">Active</Badge>;
    case 'USED': return <Badge variant="secondary">Used</Badge>;
    case 'EXPIRED': return <Badge variant="destructive" className="bg-destructive/10 text-destructive border-transparent">Expired</Badge>;
    default: return <Badge variant="outline">{status}</Badge>;
  }
};

export default function TicketsPage() {
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [isEditingPrice, setIsEditingPrice] = useState(false);
  const [newPrice, setNewPrice] = useState('10.00'); // Assuming $10 base default if not found
  const { toast } = useToast();
  const queryClient = useQueryClient();

  const { data: tickets, isLoading } = useListAdminTickets(
    { status: statusFilter !== 'ALL' ? statusFilter : undefined },
    { 
      query: { 
        queryKey: getListAdminTicketsQueryKey({ status: statusFilter !== 'ALL' ? statusFilter : undefined }) 
      } 
    }
  );

  const updatePriceMutation = useUpdateTicketPrice();

  const handleUpdatePrice = () => {
    const priceCents = Math.round(parseFloat(newPrice) * 100);
    if (isNaN(priceCents) || priceCents <= 0) {
      toast({ title: "Invalid price", variant: "destructive" });
      return;
    }

    updatePriceMutation.mutate(
      { data: { price: priceCents } },
      {
        onSuccess: () => {
          toast({ title: "Global ticket price updated" });
          setIsEditingPrice(false);
        },
        onError: () => {
          toast({ title: "Error updating price", variant: "destructive" });
        }
      }
    );
  };

  return (
    <div className="p-8 max-w-[1600px] mx-auto space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Tickets</h1>
          <p className="text-muted-foreground">Manage platform tickets and global pricing.</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card className="md:col-span-1 shadow-sm border-l-4 border-l-primary">
          <CardHeader>
            <CardTitle className="text-lg flex items-center gap-2">
              <TicketIcon className="w-5 h-5 text-primary" />
              Global Ticket Price
            </CardTitle>
            <CardDescription>Price for all newly generated tickets</CardDescription>
          </CardHeader>
          <CardContent>
            {isEditingPrice ? (
              <div className="flex items-center gap-2">
                <div className="relative flex-1">
                  <span className="absolute left-3 top-2 text-muted-foreground">$</span>
                  <Input 
                    type="number" 
                    step="0.01" 
                    min="0"
                    value={newPrice}
                    onChange={(e) => setNewPrice(e.target.value)}
                    className="pl-7 font-mono"
                  />
                </div>
                <Button size="icon" onClick={handleUpdatePrice} disabled={updatePriceMutation.isPending}>
                  <Check className="w-4 h-4" />
                </Button>
                <Button size="icon" variant="outline" onClick={() => setIsEditingPrice(false)}>
                  <X className="w-4 h-4 text-destructive" />
                </Button>
              </div>
            ) : (
              <div className="flex items-center justify-between">
                <div className="text-3xl font-bold font-mono">
                  {/* Ideally the API would return the current global price, using the input state as a placeholder */}
                  ${newPrice}
                </div>
                <Button variant="outline" size="sm" onClick={() => setIsEditingPrice(true)}>
                  <Edit2 className="w-4 h-4 mr-2" /> Edit Price
                </Button>
              </div>
            )}
          </CardContent>
        </Card>

        <div className="md:col-span-2 flex flex-col justify-end bg-card p-4 rounded-lg border shadow-sm">
          <div className="flex items-center gap-2 w-full sm:w-auto mt-auto">
            <span className="text-sm font-medium text-muted-foreground">Filter Tickets:</span>
            <Select 
              value={statusFilter} 
              onValueChange={(val) => setStatusFilter(val)}
            >
              <SelectTrigger className="w-[180px] bg-background">
                <SelectValue placeholder="All Statuses" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">All Statuses</SelectItem>
                <SelectItem value="ACTIVE">Active</SelectItem>
                <SelectItem value="USED">Used</SelectItem>
                <SelectItem value="EXPIRED">Expired</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>
      </div>

      <div className="bg-card rounded-lg border shadow-sm overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Code</TableHead>
              <TableHead>User ID</TableHead>
              <TableHead>Price Paid</TableHead>
              <TableHead>Payment Ref</TableHead>
              <TableHead>Expires</TableHead>
              <TableHead className="text-right">Status</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              Array.from({ length: 5 }).map((_, i) => (
                <TableRow key={i}>
                  <TableCell><Skeleton className="h-6 w-[120px]" /></TableCell>
                  <TableCell><Skeleton className="h-6 w-[150px]" /></TableCell>
                  <TableCell><Skeleton className="h-6 w-[80px]" /></TableCell>
                  <TableCell><Skeleton className="h-6 w-[120px]" /></TableCell>
                  <TableCell><Skeleton className="h-6 w-[120px]" /></TableCell>
                  <TableCell><Skeleton className="h-6 w-[80px] ml-auto" /></TableCell>
                </TableRow>
              ))
            ) : !tickets || tickets.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="text-center py-10 text-muted-foreground">
                  No tickets found matching criteria.
                </TableCell>
              </TableRow>
            ) : (
              tickets.map((ticket) => (
                <TableRow key={ticket.id}>
                  <TableCell>
                    <span className="font-mono font-medium tracking-wider bg-muted px-2 py-1 rounded border">
                      {ticket.code || ticket.id.substring(0,8)}
                    </span>
                  </TableCell>
                  <TableCell>
                    <span className="font-mono text-sm text-muted-foreground">
                      {ticket.userId ? ticket.userId.substring(0,12) + '...' : '-'}
                    </span>
                  </TableCell>
                  <TableCell>
                    {ticket.pricePaid ? (
                      <span className="font-mono">${(parseInt(ticket.pricePaid) / 100).toFixed(2)}</span>
                    ) : (
                      <span className="text-muted-foreground">-</span>
                    )}
                  </TableCell>
                  <TableCell>
                    <span className="font-mono text-xs text-muted-foreground truncate max-w-[120px] inline-block">
                      {ticket.paymentReference || '-'}
                    </span>
                  </TableCell>
                  <TableCell>
                    {ticket.expiresAt ? (
                      <span className="text-sm">
                        {format(new Date(ticket.expiresAt), 'MMM d, yyyy')}
                      </span>
                    ) : (
                      <span className="text-muted-foreground text-sm">Never</span>
                    )}
                  </TableCell>
                  <TableCell className="text-right">
                    {getStatusBadge(ticket.status)}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}