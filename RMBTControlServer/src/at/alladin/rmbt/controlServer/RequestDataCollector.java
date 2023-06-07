/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package at.alladin.rmbt.controlServer;

import at.alladin.rmbt.GeoIPHelper;
import com.google.common.net.InetAddresses;
import eu.bitwalker.useragentutils.UserAgent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.data.ClientInfo;
import org.restlet.data.Language;
import org.restlet.data.Preference;
import org.restlet.engine.header.Header;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.resource.Get;
import org.restlet.util.Series;

import java.util.List;

public class RequestDataCollector extends ServerResource {
    @Get("json")
    public String request(final String entity) throws JSONException {
        addAllowOrigin();

        final JSONObject answer = new JSONObject();

        final Request request = getRequest();
        final ClientInfo clientInfo = request.getClientInfo();
        final String uaString = clientInfo.getAgent();
        final String ip = getIP();
        answer.put("ip", ip);
        answer.put("country_geoip", GeoIPHelper.getInstance().lookupCountry(InetAddresses.forString(ip)));
        answer.put("port", clientInfo.getPort());

        // ONT-210 - BE: "Model" in Edge browser says Chrome
        UserAgent aagent = UserAgent.parseUserAgentString(ServletUtils.getRequest(request).getHeader("User-Agent"));
        if(uaString != null && !uaString.isEmpty() && uaString.contains("Edg/")) {
            answer.put("product", "Microsoft Edge");
            answer.put("family", "Microsoft Edge");
        } else {
            answer.put("product", aagent.getBrowser().getGroup().getName());
            answer.put("family", aagent.getBrowser().getGroup().getName());
        }
        answer.put("version", aagent.getBrowserVersion().getMajorVersion());
        answer.put("version_minor", aagent.getBrowserVersion().getMinorVersion());
        answer.put("category", aagent.getOperatingSystem().getDeviceType().getName().toUpperCase());
        answer.put("os", aagent.getOperatingSystem().getGroup().getName());

//        UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
//        ReadableUserAgent agent = parser.parse(uaString);
//
//        answer.put("product", agent.getName());
//        answer.put("version", agent.getVersionNumber().getMajor());
//        answer.put("version_minor", agent.getVersionNumber().getMinor());
//        answer.put("category", agent.getDeviceCategory().getCategory());
//        answer.put("os", agent.getOperatingSystem().getFamilyName());
//        answer.put("family", agent.getFamily());
        answer.put("agent", uaString);
        //answer.put("url", getURL().toString());

        final List<Preference<Language>> acceptedLanguages = clientInfo.getAcceptedLanguages();
        final JSONArray languages = new JSONArray();
        answer.put("languages", languages);
        for (int i = 0; i < acceptedLanguages.size(); i++) {
            final Preference<Language> preference = acceptedLanguages.get(i);
            final Language language = preference.getMetadata();
            languages.put(language.getName());
        }

        final JSONObject headersObj = new JSONObject();
        answer.put("headers", headersObj);

        @SuppressWarnings("unchecked") final Series<Header> headers = (Series<Header>) request.getAttributes().get("org.restlet.http.headers");

        if (headers != null)
            for (final Header h : headers) {
                final String name = h.getName();
                if (!name.equalsIgnoreCase("X-Real-IP") && !name.equalsIgnoreCase("X-Real-URL"))
                    headersObj.put(name, h.getValue());
            }

        return answer.toString();
    }
}
