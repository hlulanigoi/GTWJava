import * as React from "react";
import { useLocation } from "wouter";
import { useCreateRide, useInitiatePayment } from "@workspace/api-client-react";
import { MapPin, CreditCard, CheckCircle2, ArrowRight, ArrowLeft, Loader2, Info, Navigation } from "lucide-react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { useQueryClient } from "@tanstack/react-query";
import { getGetMyRidesQueryKey } from "@workspace/api-client-react";

const step1Schema = z.object({
  ride_type: z.enum(["ON_DEMAND", "SCHEDULED"]),
  pickup_address: z.string().min(5, "Pickup address is required"),
  destination_address: z.string().min(5, "Destination is required"),
  passenger_count: z.coerce.number().min(1).max(8),
  luggage_size: z.enum(["NONE", "SMALL", "MEDIUM", "LARGE"]),
  notes: z.string().optional(),
});

type Step1Data = z.infer<typeof step1Schema>;

export default function RideNew() {
  const [, setLocation] = useLocation();
  const queryClient = useQueryClient();
  const [step, setStep] = React.useState(1);
  const [rideData, setRideData] = React.useState<Step1Data | null>(null);
  const [paymentRef, setPaymentRef] = React.useState<string | null>(null);
  
  const initPaymentMut = useInitiatePayment();
  const createMut = useCreateRide();

  const { register, handleSubmit, formState: { errors }, watch } = useForm<Step1Data>({
    resolver: zodResolver(step1Schema),
    defaultValues: { ride_type: "ON_DEMAND", passenger_count: 1, luggage_size: "NONE" }
  });

  const onStep1Submit = (data: Step1Data) => {
    setRideData(data);
    
    // Estimate dummy amount
    const amount = 15.00 + (data.passenger_count > 2 ? 5 : 0);
    
    initPaymentMut.mutate({
      data: { amount, purpose: "RIDE" }
    }, {
      onSuccess: (res) => {
        setPaymentRef(res.reference);
        setStep(2);
      },
      onError: (err: any) => {
        alert("Failed to initiate payment: " + (err.message || "Unknown error"));
      }
    });
  };

  const handleSubmitFinal = () => {
    if (!rideData || !paymentRef) return;
    
    createMut.mutate({
      data: {
        ...rideData,
        payment_reference: paymentRef
      }
    }, {
      onSuccess: (res) => {
        queryClient.invalidateQueries({ queryKey: getGetMyRidesQueryKey() });
        setLocation(`/rides/${res.id}`);
      },
      onError: (err: any) => {
        alert("Failed to request ride: " + (err.message || "Unknown error"));
      }
    });
  };

  return (
    <div className="p-6 md:p-8 max-w-3xl mx-auto">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-foreground">Book a Ride</h1>
        <p className="text-muted-foreground mt-1">Request a driver to take you where you need to go.</p>
      </div>

      <div className="flex items-center justify-between mb-8 relative">
        <div className="absolute left-0 right-0 top-1/2 h-0.5 bg-secondary -z-10 transform -translate-y-1/2"></div>
        <div className="absolute left-0 top-1/2 h-0.5 bg-primary -z-10 transform -translate-y-1/2 transition-all duration-300" style={{ width: `${((step - 1) / 2) * 100}%` }}></div>
        
        {[
          { num: 1, label: "Details", icon: Navigation },
          { num: 2, label: "Payment", icon: CreditCard },
          { num: 3, label: "Review", icon: CheckCircle2 }
        ].map((s) => (
          <div key={s.num} className="flex flex-col items-center bg-background px-2">
            <div className={`h-10 w-10 rounded-full flex items-center justify-center border-2 font-medium transition-colors ${
              step >= s.num ? "bg-primary border-primary text-primary-foreground" : "bg-card border-secondary text-muted-foreground"
            }`}>
              <s.icon className="h-5 w-5" />
            </div>
            <span className={`text-xs font-medium mt-2 ${step >= s.num ? "text-foreground" : "text-muted-foreground"}`}>
              {s.label}
            </span>
          </div>
        ))}
      </div>

      <div className="bg-card border border-card-border rounded-xl shadow-sm overflow-hidden">
        {step === 1 && (
          <form onSubmit={handleSubmit(onStep1Submit)} className="p-6 md:p-8 space-y-6">
            <div className="space-y-4">
              <h2 className="text-lg font-semibold flex items-center gap-2 border-b border-card-border pb-2">
                <MapPin className="h-5 w-5 text-primary" /> Route
              </h2>
              
              <div>
                <label className="block text-sm font-medium mb-1">Pickup Address</label>
                <input
                  {...register("pickup_address")}
                  className="w-full px-4 py-2 bg-background border border-input rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
                  placeholder="Where are you?"
                />
                {errors.pickup_address && <p className="text-destructive text-sm mt-1">{errors.pickup_address.message}</p>}
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-1">Destination Address</label>
                <input
                  {...register("destination_address")}
                  className="w-full px-4 py-2 bg-background border border-input rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
                  placeholder="Where to?"
                />
                {errors.destination_address && <p className="text-destructive text-sm mt-1">{errors.destination_address.message}</p>}
              </div>
            </div>

            <div className="space-y-4 pt-4">
              <h2 className="text-lg font-semibold flex items-center gap-2 border-b border-card-border pb-2">
                <Navigation className="h-5 w-5 text-primary" /> Ride Details
              </h2>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium mb-1">Ride Type</label>
                  <select
                    {...register("ride_type")}
                    className="w-full px-4 py-2 bg-background border border-input rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
                  >
                    <option value="ON_DEMAND">On Demand (Now)</option>
                    <option value="SCHEDULED">Scheduled (Later)</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1">Passengers</label>
                  <input
                    {...register("passenger_count")}
                    type="number" min="1" max="8"
                    className="w-full px-4 py-2 bg-background border border-input rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
                  />
                </div>
                <div className="md:col-span-2">
                  <label className="block text-sm font-medium mb-1">Luggage</label>
                  <select
                    {...register("luggage_size")}
                    className="w-full px-4 py-2 bg-background border border-input rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
                  >
                    <option value="NONE">None</option>
                    <option value="SMALL">Small (Backpack)</option>
                    <option value="MEDIUM">Medium (Cabin Bag)</option>
                    <option value="LARGE">Large (Suitcase)</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium mb-1">Notes for Driver (Optional)</label>
                <textarea
                  {...register("notes")}
                  className="w-full px-4 py-2 bg-background border border-input rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent transition-all min-h-[80px]"
                  placeholder="e.g., I'm waiting near the north entrance."
                />
              </div>
            </div>

            <div className="flex justify-end pt-6 border-t border-card-border">
              <button
                type="submit"
                disabled={initPaymentMut.isPending}
                className="px-6 py-2.5 bg-primary text-primary-foreground font-medium rounded-lg hover:bg-primary/90 transition-colors flex items-center gap-2"
              >
                {initPaymentMut.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : "Continue to Payment"}
                {!initPaymentMut.isPending && <ArrowRight className="h-4 w-4" />}
              </button>
            </div>
          </form>
        )}

        {step === 2 && (
          <div className="p-6 md:p-8 space-y-6">
            <div className="text-center mb-6">
              <div className="h-16 w-16 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center mx-auto mb-4">
                <CreditCard className="h-8 w-8" />
              </div>
              <h2 className="text-2xl font-bold text-foreground">Secure Your Ride</h2>
              <p className="text-muted-foreground mt-2 max-w-md mx-auto">Please transfer the estimated fare to confirm your booking.</p>
            </div>

            <div className="bg-secondary/50 border border-secondary rounded-xl p-6 text-center space-y-2">
              <p className="text-sm font-medium text-muted-foreground uppercase tracking-wider">Payment Reference</p>
              <p className="text-3xl font-mono font-bold text-primary tracking-widest">{paymentRef}</p>
            </div>

            <div className="bg-amber-50 dark:bg-amber-900/10 border border-amber-200 dark:border-amber-800 rounded-xl p-4 flex gap-3">
              <Info className="h-5 w-5 text-amber-600 dark:text-amber-500 shrink-0 mt-0.5" />
              <div className="text-sm text-amber-900 dark:text-amber-200">
                <p className="font-medium mb-1">Bank Transfer Details</p>
                <p>Bank: Platform Bank Ltd</p>
                <p>Account: 1234-5678-9012</p>
                <p>Include the reference code above.</p>
              </div>
            </div>

            <div className="flex justify-between pt-6 border-t border-card-border">
              <button onClick={() => setStep(1)} className="px-6 py-2.5 text-foreground font-medium rounded-lg hover:bg-secondary transition-colors flex items-center gap-2">
                <ArrowLeft className="h-4 w-4" /> Back
              </button>
              <button onClick={() => setStep(3)} className="px-6 py-2.5 bg-primary text-primary-foreground font-medium rounded-lg hover:bg-primary/90 transition-colors flex items-center gap-2">
                I have paid <ArrowRight className="h-4 w-4" />
              </button>
            </div>
          </div>
        )}

        {step === 3 && rideData && (
          <div className="p-6 md:p-8 space-y-6">
            <h2 className="text-2xl font-bold text-foreground mb-6">Review Request</h2>
            
            <div className="space-y-6">
              <div className="grid grid-cols-2 gap-y-4 gap-x-8 text-sm">
                <div>
                  <p className="text-muted-foreground mb-1">Pickup</p>
                  <p className="font-medium text-foreground">{rideData.pickup_address}</p>
                </div>
                <div>
                  <p className="text-muted-foreground mb-1">Destination</p>
                  <p className="font-medium text-foreground">{rideData.destination_address}</p>
                </div>
                <div>
                  <p className="text-muted-foreground mb-1">Passengers & Luggage</p>
                  <p className="font-medium text-foreground">{rideData.passenger_count} pass., {rideData.luggage_size.toLowerCase()} luggage</p>
                </div>
                <div>
                  <p className="text-muted-foreground mb-1">Type</p>
                  <p className="font-medium text-foreground">{rideData.ride_type.replace('_', ' ')}</p>
                </div>
              </div>
            </div>

            <div className="flex justify-between pt-6 border-t border-card-border mt-8">
              <button onClick={() => setStep(2)} disabled={createMut.isPending} className="px-6 py-2.5 text-foreground font-medium rounded-lg hover:bg-secondary transition-colors flex items-center gap-2 disabled:opacity-50">
                <ArrowLeft className="h-4 w-4" /> Back
              </button>
              <button onClick={handleSubmitFinal} disabled={createMut.isPending} className="px-8 py-2.5 bg-primary text-primary-foreground font-medium rounded-lg hover:bg-primary/90 transition-colors flex items-center gap-2 shadow-sm">
                {createMut.isPending ? <Loader2 className="h-5 w-5 animate-spin" /> : <CheckCircle2 className="h-5 w-5" />}
                Confirm Booking
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
