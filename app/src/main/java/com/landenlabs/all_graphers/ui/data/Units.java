/*
 * Copyright © 2024 The Weather Company. All rights reserved.
 */

package com.landenlabs.all_graphers.ui.data;


public enum Units {

    Speed() {
        @Override
        public String shortText(boolean metric) {
            return metric ? kph : mph;      // TODO - store in string files ?
        }

        @Override
        public float convert(float value, boolean inMetric, boolean outMetric) {
            if (inMetric == outMetric) return value;
            return (inMetric) ? kphToMph(value) : mphToKph(value);
        }
    },
    SpeedKnots() {
        @Override
        public String shortText(boolean metric) {
            return metric ? kph : knots;      // TODO - store in string files ?
        }

        @Override
        public float convert(float value, boolean inMetric, boolean outMetric) {
            if (inMetric == outMetric) return value;
            return (inMetric) ? mpsToKnots(value) : knotsToMps(value);
        }
    },
    Temperature() {
        @Override
        public String shortText(boolean metric) {
            return metric ? degC : degF;      // TODO - store in string files ?
        }

        @Override
        public float convert(float value, boolean inMetric, boolean outMetric) {
            if (inMetric == outMetric) return value;
            return (inMetric) ? celsiusToFahrenheit(value) : fahrenheitToCelsius(value);
        }
    },
    TemperatureKelvin() {
        @Override
        public String shortText(boolean metric) {
            return metric ? degC : degF;      // TODO - store in string files ?
        }

        @Override
        public float convert(float kelvin, boolean ignored, boolean outMetric) {
            return (outMetric) ? kelvinToCelsius(kelvin) : celsiusToFahrenheit(kelvinToCelsius(kelvin));
        }
    },
    Rain() {
        @Override
        public String shortText(boolean metric) {
            return metric ? cm : in;      // TODO - store in string files ?
        }

        @Override
        public float convert(float value, boolean inMetric, boolean outMetric) {
            if (inMetric == outMetric) return value;
            return (inMetric) ? mmToInches(value) : inchesToMm(value);
        }
    },
    Snow() {
        @Override
        public String shortText(boolean metric) {
            return metric ? mm : in;      // TODO - store in string files ?
        }

        @Override
        public float convert(float value, boolean inMetric, boolean outMetric) {
            if (inMetric == outMetric) return value;
            return (inMetric) ? cmToInches(value) : inchesToCm(value);
        }
    },
    Percent() {
        @Override
        public String shortText(boolean metric) {
            return percent;
        }

        @Override
        public float convert(float value, boolean ignore1, boolean ignore2) {
            return value;
        }
    },
    None() {
        @Override
        public String shortText(boolean metric) {
            return none;
        }

        @Override
        public float convert(float value, boolean ignore1, boolean ignore2) {
            return value;
        }
    };

    private static final String kph = "km/h";
    private static final String mph = "mph";
    private static final String knots = "kt";
    private static final String degC = "°";
    private static final String degF = "°";
    private static final String in = "in";
    private static final String cm = "cm";
    private static final String mm = "mm";
    private static final String percent = "%";
    private static final String none = "";


    public static float asMph(float val, boolean metric) {
        return metric ? kphToMph(val) : val;
    }

    public static float asMiles(float val, boolean metric) {
        return metric ? kphToMph(val) : val;
    }

    static float mphToKph(float mph) {
        return mph * 1.60934f;
    }

    public static float mpsToKph(float metersPerSec) {
        return metersPerSec * 3600 / 1000;
    }
    static float kphToMph(float kph) {
        return kph / 1.60934f;
    }

    public static final float KNOTS_PER_MPS = 1.94384f;
    public static float mpsToKnots(float mps) {
        return mps * KNOTS_PER_MPS;
    }
    public static float knotsToMps(float knots) {
        return knots / KNOTS_PER_MPS;
    }

    public static float asFahrenheit(float val, boolean metric) {
        return metric ? celsiusToFahrenheit(val) : val;
    }

    public static float asCelsius(float val, boolean metric) {
        return metric ? val : fahrenheitToCelsius(val);
    }

    static float celsiusToFahrenheit(float celsius) {
        return 1.8f * celsius + 32;
    }
    static float kelvinToCelsius(float kelvin) {
        return kelvin - 273.15f;
    }

    static float fahrenheitToCelsius(float fahrenheit) {
        return (fahrenheit - 32) / 1.8f;
    }

    static float mmToInches(float value) {
        return value * 0.0393701f;
    }

    static float cmToInches(float value) {
        return value * 0.393701f;
    }

    static float inchesToMm(float value) {
        return value * 2.54f;
    }

    static float inchesToCm(float value) {
        return value * 25.4f;
    }

//    public static void init(@NonNull Context context) {
//        // TODO - lookup strings from string resource file for those which may change in different languages.
//        kph = context.getString(R.string.unit_kph);
//        mph = context.getString(R.string.unit_mph);
//        degC = context.getString(R.string.unit_deg_c);
//        degF = context.getString(R.string.unit_deg_f);
//        in = context.getString(R.string.unit_in);
//        cm = context.getString(R.string.unit_cm);
//        mm = context.getString(R.string.unit_mm);
//    }

    public abstract String shortText(boolean b);

    public abstract float convert(float asFloat, boolean inMetric, boolean outMetric);

}