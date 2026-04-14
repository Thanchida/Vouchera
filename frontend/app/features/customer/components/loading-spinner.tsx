export function LoadingSpinner() {
  return (
    <div className="flex items-center justify-center py-10 text-zinc-500" role="status" aria-live="polite">
      <div className="h-9 w-9 animate-spin rounded-full border-2 border-zinc-200 border-t-zinc-900" />
      <span className="sr-only">Loading</span>
    </div>
  );
}