package com.dc.bale.service;

import com.dc.bale.component.HttpClient;
import com.dc.bale.model.OAuthToken;
import com.dc.bale.model.TokenRS;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@Ignore("TODO: Fix")
public class OAuthServiceTest {
    private ConfigService configService = mock(ConfigService.class);
    private HttpClient httpClient = mock(HttpClient.class);
    private OAuthService oAuthService = new OAuthService(configService, httpClient);

    private OAuthToken result;

    @Test
    public void testTokenRetrieved() {
        givenCredentials();
        givenTokenRS("testToken1", 60);
        whenGetOAuthToken();
        thenHttpClientIsCalledOnce();
        thenTokenMatches("testToken1");
    }

    @Test
    public void testTokenReused() {
        givenCredentials();
        givenTokenRS("testToken2", 60);
        whenGetOAuthToken();
        givenTokenRS("testToken3", 60);
        whenGetOAuthToken();
        thenTokenMatches("testToken2");
    }

    @Test
    public void testTokenNotReusedWhenExpired() {
        givenCredentials();
        givenTokenRS("testToken4", 1);
        whenGetOAuthToken();
        sleepTwoSeconds();
        givenTokenRS("testToken5", 60);
        whenGetOAuthToken();
        thenTokenMatches("testToken5");
    }

    private void givenCredentials() {
        when(configService.getConfig(eq("clientID"))).thenReturn("92120405-88f1-410d-8fe9-80ba8ab1e806");
        when(configService.getConfig(eq("clientSecret"))).thenReturn("9UhaNbmSnaTzEaE0UbWR2ZpgK6Nvufqxwj2AxOM9");
    }

    private void givenTokenRS(String token, int expiresIn) {
        TokenRS tokenRS = mock(TokenRS.class);
        when(tokenRS.getAccessToken()).thenReturn(token);
        when(tokenRS.getExpiresIn()).thenReturn(expiresIn);
        when(httpClient.multipart(anyString(), anyString(), anyString(), anyString(), any())).thenReturn(tokenRS);
    }

    private void whenGetOAuthToken() {
        result = oAuthService.getOAuthToken();
    }

    private void thenHttpClientIsCalledOnce() {
        verify(httpClient, times(1)).multipart(anyString(), anyString(), anyString(), anyString(), any());
    }

    private void thenTokenMatches(String token) {
        Assert.assertEquals(token, result.getToken());
    }

    private void sleepTwoSeconds() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Assert.fail("Unable to sleep: " + e.getLocalizedMessage());
        }
    }
}
