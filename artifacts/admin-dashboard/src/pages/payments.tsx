import React, { useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { 
  useListPendingPayments, 
  getListPendingPaymentsQueryKey,
  useVerifyPayment,
  useRejectPayment
} from '@workspace/api-client-react';
import { 
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow 
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { Badge } from '@/components/ui/badge';
import { format } from 'date-fns';
import { Check, X, CreditCard, ExternalLink } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";

export default function PaymentsPage() {
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const [rejectDialog, setRejectDialog] = useState<{ isOpen: boolean; ref: string | null }>({ isOpen: false, ref: null });
  const [rejectReason, setRejectReason] = useState('');

  const { data: payments, isLoading } = useListPendingPayments({
    query: { queryKey: getListPendingPaymentsQueryKey() }
  });

  const verifyMutation = useVerifyPayment();
  const rejectMutation = useRejectPayment();

  const handleVerify = (ref: string) => {
    verifyMutation.mutate(
      { ref },
      {
        onSuccess: () => {
          toast({ title: "Payment verified successfully" });
          queryClient.invalidateQueries({ queryKey: getListPendingPaymentsQueryKey() });
          queryClient.invalidateQueries({ queryKey: ['/api/admin/stats'] });
        },
        onError: () => {
          toast({ title: "Error verifying payment", variant: "destructive" });
        }
      }
    );
  };

  const submitReject = () => {
    if (!rejectDialog.ref) return;
    
    rejectMutation.mutate(
      { ref: rejectDialog.ref, data: { reason: rejectReason || undefined } },
      {
        onSuccess: () => {
          toast({ title: "Payment rejected" });
          setRejectDialog({ isOpen: false, ref: null });
          setRejectReason('');
          queryClient.invalidateQueries({ queryKey: getListPendingPaymentsQueryKey() });
          queryClient.invalidateQueries({ queryKey: ['/api/admin/stats'] });
        },
        onError: () => {
          toast({ title: "Error rejecting payment", variant: "destructive" });
        }
      }
    );
  };

  return (
    <div className="p-8 max-w-[1600px] mx-auto space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Pending Payments</h1>
        <p className="text-muted-foreground">Verify manual payments and transfers.</p>
      </div>

      {!isLoading && payments && payments.length === 0 && (
        <Alert className="bg-muted/50 border-primary/20">
          <Check className="h-4 w-4 text-primary" />
          <AlertTitle>Inbox zero!</AlertTitle>
          <AlertDescription>
            All payments have been verified. There are no pending transactions.
          </AlertDescription>
        </Alert>
      )}

      <div className="bg-card rounded-lg border shadow-sm overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Reference</TableHead>
              <TableHead>User ID</TableHead>
              <TableHead>Type</TableHead>
              <TableHead>Amount</TableHead>
              <TableHead>Date Submitted</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              Array.from({ length: 4 }).map((_, i) => (
                <TableRow key={i}>
                  <TableCell><Skeleton className="h-6 w-[150px]" /></TableCell>
                  <TableCell><Skeleton className="h-6 w-[120px]" /></TableCell>
                  <TableCell><Skeleton className="h-6 w-[80px]" /></TableCell>
                  <TableCell><Skeleton className="h-6 w-[80px]" /></TableCell>
                  <TableCell><Skeleton className="h-6 w-[120px]" /></TableCell>
                  <TableCell><Skeleton className="h-8 w-[160px] ml-auto" /></TableCell>
                </TableRow>
              ))
            ) : payments && payments.length > 0 ? (
              payments.map((payment) => (
                <TableRow key={payment.id}>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      <CreditCard className="w-4 h-4 text-muted-foreground" />
                      <span className="font-mono font-medium tracking-wide">{payment.reference}</span>
                    </div>
                  </TableCell>
                  <TableCell>
                    <span className="font-mono text-sm text-muted-foreground">
                      {payment.userId ? payment.userId.substring(0, 12) + '...' : 'Unknown'}
                    </span>
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline" className={payment.type === 'TICKET' ? "bg-purple-50 text-purple-700 border-purple-200" : ""}>
                      {payment.type}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <span className="font-mono text-lg font-bold">
                      ${(parseInt(payment.amount) / 100).toFixed(2)}
                    </span>
                  </TableCell>
                  <TableCell>
                    <div className="text-sm">
                      {payment.createdAt ? format(new Date(payment.createdAt), 'MMM d, yyyy') : 'Unknown'}
                    </div>
                    <div className="text-xs text-muted-foreground mt-0.5">
                      {payment.createdAt ? format(new Date(payment.createdAt), 'HH:mm:ss') : ''}
                    </div>
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      <Button 
                        variant="outline" 
                        size="sm" 
                        className="text-destructive hover:bg-destructive hover:text-destructive-foreground"
                        onClick={() => setRejectDialog({ isOpen: true, ref: payment.reference })}
                        disabled={verifyMutation.isPending || rejectMutation.isPending}
                      >
                        <X className="w-4 h-4 mr-1" /> Reject
                      </Button>
                      <Button 
                        size="sm" 
                        className="bg-emerald-600 hover:bg-emerald-700 text-white"
                        onClick={() => handleVerify(payment.reference)}
                        disabled={verifyMutation.isPending || rejectMutation.isPending}
                      >
                        <Check className="w-4 h-4 mr-1" /> Verify
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))
            ) : null}
          </TableBody>
        </Table>
      </div>

      <Dialog open={rejectDialog.isOpen} onOpenChange={(open) => !open && setRejectDialog({ isOpen: false, ref: null })}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Reject Payment</DialogTitle>
            <DialogDescription>
              Are you sure you want to reject payment reference <span className="font-mono">{rejectDialog.ref}</span>? 
              This action cannot be undone.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="reason">Rejection Reason (Optional)</Label>
              <Input 
                id="reason" 
                placeholder="e.g. Invalid reference number, amount mismatch" 
                value={rejectReason}
                onChange={(e) => setRejectReason(e.target.value)}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setRejectDialog({ isOpen: false, ref: null })}>Cancel</Button>
            <Button variant="destructive" onClick={submitReject} disabled={rejectMutation.isPending}>
              Reject Payment
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}