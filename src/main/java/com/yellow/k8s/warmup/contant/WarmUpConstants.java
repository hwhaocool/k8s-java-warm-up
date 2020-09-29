package com.yellow.k8s.warmup.contant;

public class WarmUpConstants {

    public enum Flag {
        Single("warm-up-single"),
        Multi("warm-up-multi"),
        ;
        private String value;

        Flag(final String value) {
            this.value = value;
        }

        public final String getValue() {
            return value;
        }
    }

    public enum FlagValue {
        On("on"),
        Off("off"),
        ;
        private String value;

        FlagValue(final String value) {
            this.value = value;
        }

        public final String getValue() {
            return value;
        }
    }

    public enum RequestType {
        Single("single"),
        Multi("multi"),
        ;
        private String value;

        RequestType(final String value) {
            this.value = value;
        }

        public final String getValue() {
            return value;
        }
    }
}
