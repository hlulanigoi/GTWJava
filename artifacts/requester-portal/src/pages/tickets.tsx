import * as React from "react";
import { useGetMyTickets, useGetTicketPrice, useInitiatePayment, usePurchaseTicket } from "@workspace/api-client-react";
import { Ticket as TicketIcon, Plus, Loader2, CreditCard, Info } from "lucide-react";
import { format } from "date-fns";
import { Badge } from "@/components/ui/badge";
import { useQueryClient } from "@tanstack/react-query";
import { getGetMyTicketsQueryKey } from "@workspace/api-client-react";

export default function TicketsList() {
  const queryClient = useQueryClient();
  const { data: tickets, isLoading } = useGetMyTickets();
  const { data: priceData } = useGetTicketPrice();
  const initPaymentMut = useInitiatePayment();
  const purchaseMut = usePurchaseTicket();
  
  const [showBuyModal, setShowBuyModal] = React.useState(false);
  const [paymentRef, setPaymentRef] = React.useState<string | null>(null);

  const startPurchase = () => {
    initPaymentMut.mutate({
      data: { amount: priceData?.price || 10, purpose: "TICKET" }
    }, {
      onSuccess: (res) => setPaymentRef(res.reference),
      onError: (err: any) => alert(err.message || "Failed to initiate payment")
    });
  };

  const completePurchase = () => {
    if (!paymentRef) return;
    purchaseMut.mutate({
      data: { payment_reference: paymentRef }
    }, {
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: getGetMyTicketsQueryKey() });
        setShowBuyModal(false);
        setPaymentRef(null);
      },
      onError: (err: any) => alert(err.message || "Failed to purchase ticket")
    });
  };

  if (isLoading) {
    return (
      <div className="flex-1 p-8 flex items-center justify-center min-h-[50vh]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  const activeTickets = tickets?.filter(t => t.status === "ACTIVE") || [];

  return (
    <div className="p-6 md:p-8 max-w-6xl mx-auto space-y-8 relative">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-foreground">My Tickets</h1>
          <p className="text-muted-foreground text-sm mt-1">Manage your carrier tickets for completing deliveries and rides.</p>
        </div>
        <button 
          onClick={() => { setShowBuyModal(true); startPurchase(); }}
          className="inline-flex items-center justify-center gap-2 px-4 py-2 bg-primary text-primary-foreground rounded-lg font-medium hover:bg-primary/90 transition-colors shadow-sm whitespace-nowrap"
        >
          <Plus className="h-4 w-4" /> Purchase Ticket
        </button>
      </div>

      {showBuyModal && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-card w-full max-w-md rounded-2xl shadow-xl overflow-hidden animate-in zoom-in-95 duration-200">
            <div className="p-6">
              <h2 className="text-xl font-bold mb-4">Purchase a Ticket</h2>
              {paymentRef ? (
                <div className="space-y-6">
                  <div className="bg-secondary/50 border border-secondary rounded-xl p-6 text-center space-y-2">
                    <p className="text-sm font-medium text-muted-foreground uppercase">Payment Reference</p>
                    <p className="text-2xl font-mono font-bold text-primary tracking-widest">{paymentRef}</p>
                    <p className="text-sm text-muted-foreground">Amount: ${priceData?.price || 10}</p>
                  </div>
                  <div className="bg-amber-50 border border-amber-200 rounded-xl p-4 flex gap-3">
                    <Info className="h-5 w-5 text-amber-600 shrink-0" />
                    <div className="text-sm text-amber-900">
                      Transfer to Platform Bank (1234-5678-9012) using this reference to complete your purchase.
                    </div>
                  </div>
                  <div className="flex justify-end gap-3 pt-4 border-t border-card-border">
                    <button onClick={() => setShowBuyModal(false)} className="px-4 py-2 hover:bg-secondary rounded-lg font-medium transition-colors">Cancel</button>
                    <button 
                      onClick={completePurchase}
                      disabled={purchaseMut.isPending}
                      className="px-4 py-2 bg-primary text-primary-foreground rounded-lg font-medium hover:bg-primary/90 transition-colors flex items-center gap-2"
                    >
                      {purchaseMut.isPending && <Loader2 className="h-4 w-4 animate-spin" />}
                      I've made the transfer
                    </button>
                  </div>
                </div>
              ) : (
                <div className="py-12 flex justify-center"><Loader2 className="h-8 w-8 animate-spin text-primary" /></div>
              )}
            </div>
          </div>
        </div>
      )}

      {tickets?.length === 0 ? (
        <div className="bg-card border border-card-border rounded-xl p-12 text-center flex flex-col items-center">
          <div className="h-16 w-16 bg-secondary rounded-full flex items-center justify-center mb-4">
            <TicketIcon className="h-8 w-8 text-muted-foreground" />
          </div>
          <h3 className="text-lg font-semibold text-foreground">No tickets yet</h3>
          <p className="text-muted-foreground mt-1 max-w-sm mb-6">
            You need tickets to operate as a carrier or driver on the platform.
          </p>
          <button 
            onClick={() => { setShowBuyModal(true); startPurchase(); }}
            className="text-primary font-medium hover:underline"
          >
            Buy your first ticket
          </button>
        </div>
      ) : (
        <div className="space-y-6">
          <div className="bg-primary/10 border border-primary/20 rounded-xl p-6 flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div className="h-12 w-12 bg-primary rounded-full flex items-center justify-center text-primary-foreground shadow-sm">
                <TicketIcon className="h-6 w-6" />
              </div>
              <div>
                <p className="text-sm font-medium text-primary mb-0.5">Active Balance</p>
                <p className="text-3xl font-bold text-foreground">{activeTickets.length} <span className="text-lg text-muted-foreground font-medium">tickets</span></p>
              </div>
            </div>
          </div>

          <div className="bg-card border border-card-border rounded-xl overflow-hidden">
            <div className="p-5 border-b border-card-border">
              <h2 className="font-semibold text-lg">Ticket History</h2>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full text-sm text-left">
                <thead className="bg-secondary text-muted-foreground text-xs uppercase font-medium">
                  <tr>
                    <th className="px-6 py-3">Ticket Code</th>
                    <th className="px-6 py-3">Purchased On</th>
                    <th className="px-6 py-3">Price Paid</th>
                    <th className="px-6 py-3 text-right">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-card-border">
                  {tickets?.map(ticket => (
                    <tr key={ticket.id} className="hover:bg-muted/30 transition-colors">
                      <td className="px-6 py-4 font-mono font-medium">{ticket.code}</td>
                      <td className="px-6 py-4 text-muted-foreground">{ticket.createdAt ? format(new Date(ticket.createdAt), "MMM d, yyyy") : "-"}</td>
                      <td className="px-6 py-4 text-muted-foreground">${ticket.pricePaid}</td>
                      <td className="px-6 py-4 text-right">
                        <Badge status={ticket.status} />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
