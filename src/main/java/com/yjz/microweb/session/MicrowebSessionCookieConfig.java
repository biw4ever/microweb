package com.yjz.microweb.session;

import com.yjz.microweb.context.MicrowebServletContext;

public class MicrowebSessionCookieConfig implements javax.servlet.SessionCookieConfig {
	

        private String name = "sessioncookies";
        private String domain;
        private String path;
        private String comment;
        private boolean httpOnly = true;
        private boolean secure;
        private MicrowebServletContext ctx;
        private int maxAge = -1;


        /**
         * Constructor
         */
        public MicrowebSessionCookieConfig(MicrowebServletContext ctx) {
            this.ctx = ctx;
        }


        /**
         * @param name the cookie name to use
         *
         * @throws IllegalStateException if the <tt>ServletContext</tt>
         * from which this <tt>SessionCookieConfig</tt> was acquired has
         * already been initialized
         */
        @Override
        public void setName(String name) {
            if (ctx.deployed) {
                throw new IllegalArgumentException("SlimServletContext has already been deployed");
            }

            this.name = name;
        }


        /**
         * @return the cookie name set via {@link #setName}, or
         * <tt>JSESSIONID</tt> if {@link #setName} was never called
         */
        @Override
        public String getName() {
            return name;
        }


        /**
         * @param domain the cookie domain to use
         *
         * @throws IllegalStateException if the <tt>ServletContext</tt>
         * from which this <tt>SessionCookieConfig</tt> was acquired has
         * already been initialized
         */
        @Override
        public void setDomain(String domain) {
            if (ctx.deployed) {
                throw new IllegalArgumentException("SlimServletContext has already been deployed");
            }

            this.domain = domain;
        }


        /**
         * @return the cookie domain set via {@link #setDomain}, or
         * <tt>null</tt> if {@link #setDomain} was never called
         */
        @Override
        public String getDomain() {
            return domain;
        }


        /**
         * @param path the cookie path to use
         *
         * @throws IllegalStateException if the <tt>ServletContext</tt>
         * from which this <tt>SessionCookieConfig</tt> was acquired has
         * already been initialized
         */
        @Override
        public void setPath(String path) {
            if (ctx.deployed) {
                throw new IllegalArgumentException("SlimServletContext has already been deployed");
            }
            this.path = path;
        }


        /**
         * @return the cookie path set via {@link #setPath}, or the context
         * path of the <tt>ServletContext</tt> from which this 
         * <tt>SessionCookieConfig</tt> was acquired if {@link #setPath}
         * was never called
         */
        @Override
        public String getPath() {
            return path;
        }


        /**
         * @param comment the cookie comment to use
         *
         * @throws IllegalStateException if the <tt>ServletContext</tt>
         * from which this <tt>SessionCookieConfig</tt> was acquired has
         * already been initialized
         */
        @Override
        public void setComment(String comment) {
            if (ctx.deployed) {
                throw new IllegalArgumentException("SlimServletContext has already been deployed");
            }

            this.comment = comment;
        }


        /**
         * @return the cookie comment set via {@link #setComment}, or
         * <tt>null</tt> if {@link #setComment} was never called
         */
        @Override
        public String getComment() {
            return comment;
        }


        /**
         * @param httpOnly true if the session tracking cookies created
         * on behalf of the <tt>ServletContext</tt> from which this
         * <tt>SessionCookieConfig</tt> was acquired shall be marked as
         * <i>HttpOnly</i>, false otherwise
         *
         * @throws IllegalStateException if the <tt>ServletContext</tt>
         * from which this <tt>SessionCookieConfig</tt> was acquired has
         * already been initialized
         */
        @Override
        public void setHttpOnly(boolean httpOnly) {
            if (ctx.deployed) {
                throw new IllegalArgumentException("SlimServletContext has already been deployed");
            }

            this.httpOnly = httpOnly;
        }


        /**
         * @return true if the session tracking cookies created on behalf of the
         * <tt>ServletContext</tt> from which this <tt>SessionCookieConfig</tt>
         * was acquired will be marked as <i>HttpOnly</i>, false otherwise
         */
        @Override
        public boolean isHttpOnly() {
            return httpOnly;
        }


        /**
         * @param secure true if the session tracking cookies created on
         * behalf of the <tt>ServletContext</tt> from which this
         * <tt>SessionCookieConfig</tt> was acquired shall be marked as
         * <i>secure</i> even if the request that initiated the corresponding
         * session is using plain HTTP instead of HTTPS, and false if they
         * shall be marked as <i>secure</i> only if the request that initiated
         * the corresponding session was also secure
         *
         * @throws IllegalStateException if the <tt>ServletContext</tt>
         * from which this <tt>SessionCookieConfig</tt> was acquired has
         * already been initialized
         */
        @Override
        public void setSecure(boolean secure) {
            if (ctx.deployed) {
                throw new IllegalArgumentException("SlimServletContext has already been deployed");
            }

            this.secure = secure;
        }


        /**
         * @return true if the session tracking cookies created on behalf of the
         * <tt>ServletContext</tt> from which this <tt>SessionCookieConfig</tt>
         * was acquired will be marked as <i>secure</i> even if the request
         * that initiated the corresponding session is using plain HTTP
         * instead of HTTPS, and false if they will be marked as <i>secure</i>
         * only if the request that initiated the corresponding session was
         * also secure
         */
        @Override
        public boolean isSecure() {
            return secure;
        }


        @Override
        public void setMaxAge(int maxAge) {
            if (ctx.deployed) {
                throw new IllegalArgumentException("SlimServletContext has already been deployed");
            }

            this.maxAge = maxAge;
        }


        @Override
        public int getMaxAge() {
            return maxAge;
        }

    }

