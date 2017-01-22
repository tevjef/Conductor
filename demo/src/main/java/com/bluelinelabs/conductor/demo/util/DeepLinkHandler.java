package com.bluelinelabs.conductor.demo.util;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DeepLinkHandler {

    private List<DeepLink> registeredLinks = new ArrayList<>();

    public void registerDeepLink(@NonNull String pattern, @NonNull DeepLinkCall call) {
        DeepLink link = new DeepLink(pattern, call);
        if (!registeredLinks.contains(link)) {
            registeredLinks.add(link);
        }
    }

    public boolean handleIntent(Intent intent) {
        Uri uri = intent.getData();
        if (uri == null || uri.getPath() == null) {
            return false;
        }

        List<String> pathSegments = uri.getPathSegments();
        if (pathSegments == null || pathSegments.size() == 0) {
            return false;
        }

        for (DeepLink deepLink : registeredLinks) {
            if (deepLink.patternSegments.size() != pathSegments.size()) {
                continue;
            }

            List<Parameter> parameters = new ArrayList<>();
            boolean matches = true;
            for (int i = 0; i < pathSegments.size(); i++) {
                String patternSegment = deepLink.patternSegments.get(i);
                String pathSegment = pathSegments.get(i);

                if (patternSegment.startsWith("{") && patternSegment.endsWith("}")) {
                    parameters.add(new Parameter(patternSegment.substring(1, patternSegment.length() - 2), pathSegment));
                } else if (!pathSegment.equals(patternSegment)) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                deepLink.call.call(parameters);
                return true;
            }
        }

        return false;
    }

    public interface DeepLinkCall {
        void call(List<Parameter> parameters);
    }

    private static class DeepLink {
        final String pattern;
        final DeepLinkCall call;
        final List<String> patternSegments;
        final Set<String> parameters;

        public DeepLink(@NonNull String pattern, @NonNull DeepLinkCall call) {
            this.pattern = pattern;
            this.call = call;

            Uri patternUri = Uri.parse(pattern);
            patternSegments = patternUri.getPathSegments();
            parameters = patternUri.getQueryParameterNames();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DeepLink deepLink = (DeepLink) o;
            return pattern.equals(deepLink.pattern);
        }
    }

    public static class Parameter {
        public final String key;
        public final String value;

        public Parameter(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

}
