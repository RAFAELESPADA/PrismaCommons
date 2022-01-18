package com.br.gabrielsilva.prismamc.commons.core.utils.fetcher;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class UUIDFetcher {
	
    private List<String> apis;
    
    public UUIDFetcher() {
        this.apis = new ArrayList<String>();
    }
    
    public void init() {
        this.apis.add("https://api.mojang.com/users/profiles/minecraft/%s");
        this.apis.add("https://api.mcuuid.com/json/uuid/%s");
        this.apis.add("https://api.minetools.eu/uuid/%s");
    }
    
    private UUID request(final String name) {
        return this.request(0, this.apis.get(0), name);
    }
    
    private UUID request(int idx, String api, final String name) {
        try {
            final URLConnection con = new URL(String.format(api, name)).openConnection();
            final JsonElement element = Core.getParser().parse(new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)));
            if (element instanceof JsonObject) {
                final JsonObject object = (JsonObject)element;
                if (object.has("error") && object.has("errorMessage")) {
                    throw new Exception(object.get("errorMessage").getAsString());
                }
                if (object.has("id")) {
                    return UUIDParser.parse(object.get("id"));
                }
                if (object.has("uuid")) {
                    final JsonObject uuid = object.getAsJsonObject("uuid");
                    if (uuid.has("formatted")) {
                        return UUIDParser.parse(object.get("formatted"));
                    }
                }
            }
        }
        catch (Exception e) {
            if (++idx < this.apis.size()) {
                api = this.apis.get(idx);
                return this.request(idx, api, name);
            }
        }
        return null;
    }
    
    public UUID getUUID(final String name) {
        if (name.matches("[a-zA-Z0-9_]{3,16}")) {
            try {
            	return request(name);
            }
            catch (Exception ex) {}
        }
        return UUIDParser.parse(name);
    }
}
