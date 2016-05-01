package com.jakedeacon.jake.maps;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Jake Deacon
 * @version 1.1
 *          Helper Class used in Async Classes to allow for code reuse as more async calls are needed
 * @since 2016-05-01
 */
public class AsyncHelper {

    public AsyncHelper() {

    }

    /**
     * Step one of URL get logic that handles the HttpRequest in doInBackground
     *
     * @param url - URL which is needed to get data from
     * @return String - converted string from InputStream opened
     */
    public static String GET(String url) {
        String result = "";

        URL u = null;
        try {
            u = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        HttpURLConnection urlConnection = null;
        if (u != null) {
            try {
                urlConnection = (HttpURLConnection) u.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (urlConnection != null) {
                InputStream in = null;
                try {
                    in = new BufferedInputStream(urlConnection.getInputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (in != null) {
                    result = convertInputStreamToString(in);
                } else {
                    return null;
                }

                urlConnection.disconnect();
            }
        }
        return result;
    }

    /**
     * Helper method that converts Inputstream to a String to be used in the 2nd part of logic for
     * doinBackground
     *
     * @param inputStream - provided from the GET method
     * @return String representation of the contents of the Inputstream
     */
    // LOGIC 2 - Type Conversion from GET
    public static String convertInputStreamToString(InputStream inputStream) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        try {
            while ((line = bufferedReader.readLine()) != null)
                result += line;
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

}
