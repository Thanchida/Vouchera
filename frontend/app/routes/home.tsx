export function meta() {
  return [
    { title: "Vouchera" },
    {
      name: "description",
      content: "Vouchera helps teams launch and manage voucher campaigns with clarity.",
    },
  ];
}

export default function Home() {
  return (
    <div className="min-h-screen bg-white text-zinc-900">
      <header className="border-b border-zinc-200/80 bg-white/95 backdrop-blur">
        <div className="mx-auto flex h-16 w-full max-w-6xl items-center justify-between px-6 sm:px-8">
          <a href="/" className="text-base font-semibold tracking-tight text-zinc-950">
            Vouchera
          </a>

          <nav aria-label="Main navigation" className="flex items-center gap-1">
            <a
              href="/"
              className="rounded-lg px-3 py-2 text-sm text-zinc-600 transition-colors hover:bg-zinc-100 hover:text-zinc-900"
            >
              Home
            </a>
            <a
              href="#about"
              className="rounded-lg px-3 py-2 text-sm text-zinc-600 transition-colors hover:bg-zinc-100 hover:text-zinc-900"
            >
              About
            </a>
            <a
              href="/login"
              className="rounded-lg px-3 py-2 text-sm text-zinc-600 transition-colors hover:bg-zinc-100 hover:text-zinc-900"
            >
              Login
            </a>
          </nav>
        </div>
      </header>

      <main className="mx-auto flex min-h-[calc(100vh-4rem)] w-full max-w-6xl items-center justify-center px-6 py-16 sm:px-8 sm:py-24">
        <section className="w-full max-w-2xl text-center">
          <p className="text-xs font-medium uppercase tracking-[0.2em] text-zinc-500">
            Voucher Campaign Platform
          </p>
          <h1 className="mt-5 text-4xl font-semibold leading-tight tracking-tight text-zinc-950 sm:text-5xl">
            Launch Better Rewards
          </h1>
          <p className="mx-auto mt-5 max-w-xl text-base leading-relaxed text-zinc-600 sm:text-lg">
            Create, publish, and track voucher campaigns with a focused workspace that keeps
            teams aligned and customers engaged.
          </p>

          <div className="mt-10 flex flex-col items-center justify-center gap-3 sm:flex-row">
            <a
              href="/register"
              className="inline-flex h-11 w-full items-center justify-center rounded-xl bg-zinc-900 px-6 text-sm font-medium text-white transition-colors hover:bg-zinc-800 sm:w-auto"
            >
              Register
            </a>
            <a
              href="/login"
              className="inline-flex h-11 w-full items-center justify-center rounded-xl border border-zinc-300 bg-white px-6 text-sm font-medium text-zinc-700 transition-colors hover:bg-zinc-100 hover:text-zinc-900 sm:w-auto"
            >
              Login
            </a>
          </div>

          <div
            id="about"
            className="mx-auto mt-14 max-w-xl rounded-2xl border border-zinc-200 bg-zinc-50 px-6 py-5 text-left"
          >
            <h2 className="text-sm font-semibold text-zinc-900">Built for clarity</h2>
            <p className="mt-2 text-sm leading-6 text-zinc-600">
              Vouchera gives your team a calm, structured place to handle campaign setup,
              voucher types, and redemption flow without noisy interfaces.
            </p>
          </div>
        </section>
      </main>
    </div>
  );
}
