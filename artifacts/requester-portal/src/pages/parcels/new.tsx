import * as React from "react";
import { useLocation } from "wouter";
import { useCreateParcel, useInitiatePayment, ParcelInput } from "@workspace/api-client-react";
import { Package, MapPin, CreditCard, CheckCircle2, ArrowRight, ArrowLeft, Loader2, Info } from "lucide-react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { useQueryClient } from "@tanstack/react-query";
import { getGetMyParcelsQueryKey } from "@workspace/api-client-react";

const step1Schema = z.object({
  description: z.string().min(3, "Description must be at least 3 characters"),
  weight: z.coerce.number().min(0.1, "Weight must be greater than 0"),
  size: z.enum(["SMALL", "MEDIUM", "LARGE"]),
  pickup_address: z.string().min(5, "Pickup address is required"),
  destination_address: z.string().min(5, "Destination is required"),
  special_instructions: z.string().optional(),
});

type Step1Data = z.infer<typeof step1Schema>;

export default function ParcelNew() {
  const [, setLocation] = useLocation();
  const queryClient = useQueryClient();
  const [step, setStep] = React.useState(1);
  const [parcelData, setParcelData] = React.useState<Step1Data | null>(null);
  const [paymentRef, setPaymentRef] = React.useState<string | null>(null);
  
  const initPaymentMut = useInitiatePayment();
  const createMut = useCreateParcel();

  const { register, handleSubmit, formState: { errors }, watch } = useForm<Step1Data>({
    resolver: zodResolver(step1Schema),
    defaultValues: { size: "MEDIUM" }
  });

  const onStep1Submit = (data: Step1Data) => {
    setParcelData(data);
    
    // Calculate dummy amount based on weight and size
    const baseFee = 5.00;
    const weightFee = data.weight * 2.50;
    const sizeFee = data.size === "LARGE" ? 10 : data.size === "MEDIUM" ? 5 : 0;
    const amount = baseFee + weightFee + sizeFee;
    
    initPaymentMut.mutate({
      data: { amount, purpose: "TICKET" } // API purpose usually RIDE or TICKET. Let's pass TICKET or if there's PARCEL, we'd use that. The schema says RIDE | TICKET. We'll use RIDE as generic service payment for now to pass validation.
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

  const handleCompletePayment = () => {
    setStep(3);
  };

  const handleSubmitFinal = () => {
    if (!parcelData || !paymentRef) return;
    
    createMut.mutate({
      data: {
        ...parcelData,
        payment_reference: paymentRef
      }
    }, {
      onSuccess: (res) => {
        queryClient.invalidateQueries({ queryKey: getGetMyParcelsQueryKey() });
        setLocation(`/parcels/${res.id}`);
      },
      onError: (err: any) => {
        alert("Failed to create parcel: " + (err.message || "Unknown error"));
      }
    });
  };

  return (
    <div className="p-6 md:p-8 max-w-3xl mx-auto">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-foreground">Send a Parcel</h1>
        <p className="text-muted-foreground mt-1">Fill out the details below to request a pickup.</p>
      </div>

      {/* Stepper */}
      <div className="flex items-center justify-between mb-8 relative">
        <div className="absolute left-0 right-0 top-1/2 h-0.5 bg-secondary -z-10 transform -translate-y-1/2"></div>
        <div className="absolute left-0 top-1/2 h-0.5 bg-primary -z-10 transform -translate-y-1/2 transition-all duration-300" style={{ width: `${((step - 1) / 2) * 100}%` }}></div>
        
        {[
          { num: 1, label: "Details", icon: Package },
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
                <Package className="h-5 w-5 text-primary" /> What are you sending?
              </h2>
              
              <div>
                <label className="block text-sm font-medium mb-1">Description</label>
                <input
                  {...register("description")}
                  className="w-full px-4 py-2 bg-background border border-input rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
                  placeholder="e.g., A box of books"
                />
                {errors.description && <p className="text-destructive text-sm mt-1">{errors.description.message}</p>}
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium mb-1">Weight (kg)</label>
                  <input
                    {...register("weight")}
                    type="number" step="0.1"
                    className="w-full px-4 py-2 bg-background border border-input rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
                    placeholder="2.5"
                  />
                  {errors.weight && <p className="text-destructive text-sm mt-1">{errors.weight.message}</p>}
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1">Size</label>
                  <select
                    {...register("size")}
                    className="w-full px-4 py-2 bg-background border border-input rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
                  >
                    <option value="SMALL">Small (Shoebox or smaller)</option>
                    <option value="MEDIUM">Medium (Microwave box)</option>
                    <option value="LARGE">Large (Moving box)</option>
                  </select>
                </div>
              </div>
            </div>

            <div className="space-y-4 pt-4">
              <h2 className="text-lg font-semibold flex items-center gap-2 border-b border-card-border pb-2">
                <MapPin className="h-5 w-5 text-primary" /> Route
              </h2>
              
              <div>
                <label className="block text-sm font-medium mb-1">Pickup Address</label>
                <input
                  {...register("pickup_address")}
                  className="w-full px-4 py-2 bg-background border border-input rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
                  placeholder="123 Sender St, City"
                />
                {errors.pickup_address && <p className="text-destructive text-sm mt-1">{errors.pickup_address.message}</p>}
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-1">Destination Address</label>
                <input
                  {...register("destination_address")}
                  className="w-full px-4 py-2 bg-background border border-input rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
                  placeholder="456 Receiver Ave, City"
                />
                {errors.destination_address && <p className="text-destructive text-sm mt-1">{errors.destination_address.message}</p>}
              </div>

              <div>
                <label className="block text-sm font-medium mb-1">Special Instructions (Optional)</label>
                <textarea
                  {...register("special_instructions")}
                  className="w-full px-4 py-2 bg-background border border-input rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent transition-all min-h-[80px]"
                  placeholder="e.g., Fragile, keep upright."
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
              <h2 className="text-2xl font-bold text-foreground">Payment Required</h2>
              <p className="text-muted-foreground mt-2 max-w-md mx-auto">Please transfer the delivery fee to our platform account to secure your request.</p>
            </div>

            <div className="bg-secondary/50 border border-secondary rounded-xl p-6 text-center space-y-2">
              <p className="text-sm font-medium text-muted-foreground uppercase tracking-wider">Payment Reference</p>
              <p className="text-3xl font-mono font-bold text-primary tracking-widest">{paymentRef}</p>
              <p className="text-sm text-muted-foreground mt-4">Include this reference in your bank transfer description.</p>
            </div>

            <div className="bg-amber-50 dark:bg-amber-900/10 border border-amber-200 dark:border-amber-800 rounded-xl p-4 flex gap-3">
              <Info className="h-5 w-5 text-amber-600 dark:text-amber-500 shrink-0 mt-0.5" />
              <div className="text-sm text-amber-900 dark:text-amber-200">
                <p className="font-medium mb-1">Bank Transfer Details</p>
                <p>Bank: Platform Bank Ltd</p>
                <p>Account: 1234-5678-9012</p>
                <p>Name: Going That Way Escrow</p>
              </div>
            </div>

            <div className="flex justify-between pt-6 border-t border-card-border">
              <button
                onClick={() => setStep(1)}
                className="px-6 py-2.5 text-foreground font-medium rounded-lg hover:bg-secondary transition-colors flex items-center gap-2"
              >
                <ArrowLeft className="h-4 w-4" /> Back
              </button>
              <button
                onClick={handleCompletePayment}
                className="px-6 py-2.5 bg-primary text-primary-foreground font-medium rounded-lg hover:bg-primary/90 transition-colors flex items-center gap-2"
              >
                I have made the transfer <ArrowRight className="h-4 w-4" />
              </button>
            </div>
          </div>
        )}

        {step === 3 && parcelData && (
          <div className="p-6 md:p-8 space-y-6">
            <h2 className="text-2xl font-bold text-foreground mb-6">Review & Submit</h2>
            
            <div className="space-y-6">
              <div className="grid grid-cols-2 gap-y-4 gap-x-8 text-sm">
                <div>
                  <p className="text-muted-foreground mb-1">Description</p>
                  <p className="font-medium text-foreground">{parcelData.description}</p>
                </div>
                <div>
                  <p className="text-muted-foreground mb-1">Weight & Size</p>
                  <p className="font-medium text-foreground">{parcelData.weight} kg, {parcelData.size}</p>
                </div>
                <div>
                  <p className="text-muted-foreground mb-1">Pickup</p>
                  <p className="font-medium text-foreground">{parcelData.pickup_address}</p>
                </div>
                <div>
                  <p className="text-muted-foreground mb-1">Destination</p>
                  <p className="font-medium text-foreground">{parcelData.destination_address}</p>
                </div>
                <div className="col-span-2">
                  <p className="text-muted-foreground mb-1">Payment Reference</p>
                  <p className="font-mono font-medium text-primary bg-secondary/50 px-2 py-1 rounded inline-block">{paymentRef}</p>
                </div>
              </div>
            </div>

            <div className="flex justify-between pt-6 border-t border-card-border mt-8">
              <button
                onClick={() => setStep(2)}
                disabled={createMut.isPending}
                className="px-6 py-2.5 text-foreground font-medium rounded-lg hover:bg-secondary transition-colors flex items-center gap-2 disabled:opacity-50"
              >
                <ArrowLeft className="h-4 w-4" /> Back
              </button>
              <button
                onClick={handleSubmitFinal}
                disabled={createMut.isPending}
                className="px-8 py-2.5 bg-primary text-primary-foreground font-medium rounded-lg hover:bg-primary/90 transition-colors flex items-center gap-2 shadow-sm"
              >
                {createMut.isPending ? <Loader2 className="h-5 w-5 animate-spin" /> : <CheckCircle2 className="h-5 w-5" />}
                Submit Request
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
