import { useState } from "react";
import { useNavigate } from "react-router";
import { request } from "~/lib/api-client";

type LoginForm = {
  email: string;
  password: string;
};

type LoginErrors = Partial<Record<keyof LoginForm, string>>;

export function meta() {
  return [
    { title: "Login | Vouchera" },
    { name: "description", content: "Log in to your Vouchera account." },
  ];
}

const initialForm: LoginForm = {
  email: "",
  password: "",
};

export default function LoginPage() {
  const [form, setForm] = useState<LoginForm>(initialForm);
  const [errors, setErrors] = useState<LoginErrors>({});
  const [submitError, setSubmitError] = useState("");
  const navigate = useNavigate();

  function setField<K extends keyof LoginForm>(key: K, value: LoginForm[K]) {
    setForm(prev => ({ ...prev, [key]: value }));
    setErrors(prev => ({ ...prev, [key]: undefined }));
  }

  function validate(values: LoginForm): LoginErrors {
    const nextErrors: LoginErrors = {};

    if (!values.email.trim()) {
      nextErrors.email = "Email is required.";
    } else if (!/^\S+@\S+\.\S+$/.test(values.email)) {
      nextErrors.email = "Enter a valid email address.";
    }

    if (!values.password) {
      nextErrors.password = "Password is required.";
    }

    return nextErrors;
  }

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const nextErrors = validate(form);
    setErrors(nextErrors);
    setSubmitError("");

    if (Object.keys(nextErrors).length > 0) {
      return;
    }

    void (async () => {
      try {
        const response = await request<{ role?: string }>("/api/auth/login", {
          method: "POST",
          body: JSON.stringify(form),
        });

        const role = response.role?.toLowerCase();

        if (role === "admin") {
          navigate("/admin");
          return;
        }

        if (role === "marketing" && form.email.trim()) {
          navigate("/marketing/dashboard");
          return;
        }

        if (role === "customer") {
          navigate("/customer");
          return;
        }

        navigate("/");
      } catch (error) {
        setSubmitError(error instanceof Error ? error.message : "Login failed.");
      }
    })();
  }

  return (
    <main className="min-h-screen bg-white px-4 py-12 sm:px-6 lg:px-8">
      <div className="mx-auto flex min-h-[calc(100vh-6rem)] w-full max-w-5xl items-center justify-center">
        <section className="w-full max-w-md rounded-2xl border border-zinc-200 bg-white p-7 shadow-sm sm:p-8">
          <h1 className="text-2xl font-semibold tracking-tight text-zinc-950">Welcome Back</h1>
          <p className="mt-2 text-sm text-zinc-600">Log in to your account</p>

          <form className="mt-7 space-y-4" onSubmit={handleSubmit} noValidate>
            {submitError ? (
              <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                {submitError}
              </div>
            ) : null}

            <div>
              <label htmlFor="email" className="mb-1.5 block text-sm font-medium text-zinc-800">
                Email
              </label>
              <input
                id="email"
                name="email"
                type="email"
                autoComplete="email"
                value={form.email}
                onChange={event => setField("email", event.target.value)}
                className="h-11 w-full rounded-xl border border-zinc-300 bg-white px-3 text-sm text-zinc-900 outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
              />
              {errors.email ? <p className="mt-1.5 text-xs text-red-600">{errors.email}</p> : null}
            </div>

            <div>
              <label htmlFor="password" className="mb-1.5 block text-sm font-medium text-zinc-800">
                Password
              </label>
              <input
                id="password"
                name="password"
                type="password"
                autoComplete="current-password"
                value={form.password}
                onChange={event => setField("password", event.target.value)}
                className="h-11 w-full rounded-xl border border-zinc-300 bg-white px-3 text-sm text-zinc-900 outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
              />
              {errors.password ? <p className="mt-1.5 text-xs text-red-600">{errors.password}</p> : null}
            </div>

            <div className="pt-0.5">
              <a href="/forgot-password" className="text-sm text-zinc-600 underline-offset-2 hover:underline">
                Forgot password?
              </a>
            </div>

            <button
              type="submit"
              className="mt-2 inline-flex h-11 w-full items-center justify-center rounded-xl bg-zinc-900 text-sm font-medium text-white transition-colors hover:bg-zinc-800"
            >
              Login
            </button>
          </form>

          <p className="mt-6 text-center text-sm text-zinc-600">
            Don&apos;t have an account?{" "}
            <a href="/register" className="font-medium text-zinc-900 underline-offset-2 hover:underline">
              Register
            </a>
          </p>
        </section>
      </div>
    </main>
  );
}
