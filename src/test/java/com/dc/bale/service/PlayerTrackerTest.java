package com.dc.bale.service;

import com.dc.bale.database.dao.MountRepository;
import com.dc.bale.database.entity.Mount;
import com.dc.bale.database.entity.Player;
import com.dc.bale.exception.MountException;
import com.dc.bale.model.AvailableMount;
import com.dc.bale.model.MountRS;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class PlayerTrackerTest {
    private static final String USSA = "Ussa Xellus";
    private static final String SYTH = "Syth Rilletta";
    private MountRepository mountRepository = mock(MountRepository.class);
    private PlayerService playerService = mock(PlayerService.class);
    private FcLoader fcLoader = mock(FcLoader.class);
    private InstanceService instanceService = mock(InstanceService.class);

    private PlayerTracker playerTracker = new PlayerTracker(mountRepository, playerService, fcLoader, instanceService);

    @Captor
    private ArgumentCaptor<List<Player>> playersCaptor;

    private List<MountRS> mountResponse;
    private Map<String, Player> playerMap = new HashMap<>();
    private List<Player> visiblePlayers = new ArrayList<>();
    private Mount mount = mock(Mount.class);
    private List<AvailableMount> availableMounts;

    @Before
    public void setup() {
        when(playerService.getPlayerMap()).thenReturn(playerMap);
        when(playerService.getVisiblePlayers()).thenReturn(visiblePlayers);
    }

    @Test
    public void testGetMounts_MountExists() {
        givenTotalMounts();
        givenPlayer(USSA, "m2");
        givenPlayer(SYTH, "m1", "m2");
        whenGetMounts();
        thenMountShouldExist();
    }

    @Test
    public void testCleanOldPlayers_OldPlayersAreRemoved() {
        givenPlayer(SYTH);
        givenFCPageContent(1);
        whenCleanOldPlayers();
        thenPlayerShouldBeDeleted();
    }

    @Test
    public void testCleanOldPlayers_ExistingPlayersAreNotRemoved() {
        givenFCPageContent(1);
        givenPlayer(USSA);
        whenCleanOldPlayers();
        thenPlayerShouldNotBeDeleted();
    }

    @Test
    public void testCleanOldPlayers_TwoPagesExistingPlayersAreNotRemoved() {
        givenFCPageContent(2);
        givenSecondFCPageContent();
        givenPlayer(USSA);
        givenPlayer(SYTH);
        whenCleanOldPlayers();
        thenPlayerShouldNotBeDeleted();
    }

    @Test
    public void testCleanOldPlayers_PageLoadFailureDoesNotDeletePlayers() {
        givenFCContentLoadFailure();
        givenPlayer(USSA);
        whenCleanOldPlayers();
        thenDeleteShouldNotBeCalled();
    }

    @Test(expected = MountException.class)
    public void testAddMount_Null() throws MountException {
        givenNullFindMountByName();
        whenAddMount();
    }

    @Test
    public void testAddMount_Valid() throws MountException {
        givenFindMountByName();
        whenAddMount();
        thenMountVisibilityShouldBe(true);
        thenMountShouldBeSaved();
    }

    @Test(expected = MountException.class)
    public void testRemoveMount_Null() throws MountException {
        givenNullFindMountById();
        whenRemoveMount();
    }

    @Test
    public void testRemoveMount_Valid() throws MountException {
        givenFindMountById();
        whenRemoveMount();
        thenMountVisibilityShouldBe(false);
        thenMountShouldBeSaved();
    }

    @Test
    public void testLoadMounts() {
        whenLoadMounts();
        thenPlayerDataShouldBeLoaded();
        thenLastUpdatedShouldBeSet();
    }

    @Test
    public void testGetAvailableMounts() {
        givenNotVisibleMounts();
        whenGetAvailableMounts();
        thenFindAllByVisibleShouldBeCalled();
        thenThereShouldBeThreeAvailableMounts();
        thenAvailableMountsShouldBeSortedByName();
    }

    private void givenNullFindMountByName() {
        when(mountRepository.findByName(anyString())).thenReturn(null);
    }

    private void givenFindMountByName() {
        when(mountRepository.findByName(anyString())).thenReturn(mount);
    }

    private void givenNullFindMountById() {
        when(mountRepository.findOne(anyLong())).thenReturn(null);
    }

    private void givenFindMountById() {
        when(mountRepository.findOne(anyLong())).thenReturn(mount);
    }

    private void givenTotalMounts() {
        List<Mount> mounts = new ArrayList<>();
        Mount mount1 = mock(Mount.class);
        when(mount1.getId()).thenReturn(1L);
        when(mount1.getName()).thenReturn("m1");
        mounts.add(mount1);
        Mount mount2 = mock(Mount.class);
        when(mount2.getId()).thenReturn(2L);
        when(mount2.getName()).thenReturn("m2");
        mounts.add(mount2);
        Mount mount3 = mock(Mount.class);
        when(mount3.getId()).thenReturn(3L);
        when(mount3.getName()).thenReturn("m3");
        mounts.add(mount3);
        when(mountRepository.findAllByVisible(eq(true))).thenReturn(mounts);
    }

    private void givenFCPageContent(int numPages) {
        when(fcLoader.getFCPageContent()).thenReturn("\t\t\t\t<li class=\"entry\"><a href=\"/lodestone/character/16341245/\" class=\"entry__bg mychara\"><div class=\"entry__flex\"><div class=\"entry__chara__face\"><img src=\"https://img2.finalfantasyxiv.com/f/26b18137662f7b443aafbcf39d8cbd29_c274370774c6bc3483cc8740805f41bcfc0_96x96.jpg?1557009670\" alt=\"\"><i class=\"entry__freecompany__online parts__online_off\"></i></div><div class=\"entry__freecompany__center\"><p class=\"entry__name\">Ussa Xellus</p><p class=\"entry__world\"><i class=\"xiv-lds xiv-lds-home-world js__tooltip\" data-tooltip=\"Home World\"></i>Zodiark&nbsp;(Light)</p><ul class=\"entry__freecompany__info\"><li><img src=\"https://img.finalfantasyxiv.com/lds/h/s/uIrHic2MOYHNS316SWOpAFgMKM.png\" width=\"20\" height=\"20\" alt=\"\"><span>Bismarck</span></li><li><i class=\"list__ic__class\"><img src=\"https://img.finalfantasyxiv.com/lds/h/m/KndG72XtCFwaq1I1iqwcmO_0zc.png\" width=\"20\" height=\"20\" alt=\"\"></i><span>70</span></li><li class=\"js__tooltip\" data-tooltip=\"Immortal Flames / First Flame Lieutenant\"><img src=\"https://img.finalfantasyxiv.com/lds/h/Z/xoUzj9-PpBilPQFcLtcsw0anac.png\" width=\"20\" height=\"20\" alt=\"\"></li></ul></div></div></a></li>");
        when(fcLoader.getNumPages(anyString())).thenReturn(numPages);
    }

    private void givenSecondFCPageContent() {
        when(fcLoader.getFCPageContent(anyInt())).thenReturn("\t\t\t\t<li class=\"entry\"><a href=\"/lodestone/character/14438652/\" class=\"entry__bg\"><div class=\"entry__flex\"><div class=\"entry__chara__face\"><img src=\"https://img2.finalfantasyxiv.com/f/5720225374a702367d5f5fe1ece5ab39_c274370774c6bc3483cc8740805f41bcfc0_96x96.jpg?1557009742\" alt=\"\"><i class=\"entry__freecompany__online parts__online_off\"></i></div><div class=\"entry__freecompany__center\"><p class=\"entry__name\">Syth Rilletta</p><p class=\"entry__world\"><i class=\"xiv-lds xiv-lds-home-world js__tooltip\" data-tooltip=\"Home World\"></i>Zodiark&nbsp;(Light)</p><ul class=\"entry__freecompany__info\"><li><img src=\"https://img.finalfantasyxiv.com/lds/h/6/p94F1j-5xhM2ySM16VNrA08qjU.png\" width=\"20\" height=\"20\" alt=\"\"><span>MilleniumFalcon</span></li><li><i class=\"list__ic__class\"><img src=\"https://img.finalfantasyxiv.com/lds/h/7/i20QvSPcSQTybykLZDbQCgPwMw.png\" width=\"20\" height=\"20\" alt=\"\"></i><span>70</span></li><li class=\"js__tooltip\" data-tooltip=\"Order of the Twin Adder / Serpent Captain\"><img src=\"https://img.finalfantasyxiv.com/lds/h/O/yMgc1Z4bAOmEzo1JWBP0hs9FQs.png\" width=\"20\" height=\"20\" alt=\"\"></li></ul></div></div><p class=\"entry__freecompany__fc-comment\">What did cured ham actually have?</p><p class=\"entry__freecompany__update\">Last Update: <span id=\"datetime-0.547483809473999\">-</span><script>document.getElementById('datetime-0.547483809473999').innerHTML = ldst_strftime(1513822922, 'YMDHM');</script></p></a></li>");
    }

    private void givenFCContentLoadFailure() {
        when(fcLoader.getFCPageContent()).thenReturn(StringUtils.EMPTY);
    }

    private void givenPlayer(String playerName, String... mountNames) {
        Player player = mock(Player.class);
        when(player.getId()).thenReturn(visiblePlayers.size() + 1L);
        when(player.getName()).thenReturn(playerName);

        Set<Mount> mounts = new HashSet<>();
        for (String mountName : mountNames) {
            Mount mount = mock(Mount.class);
            when(mount.getId()).thenReturn(mounts.size() + 1L);
            when(mount.getName()).thenReturn(mountName);
            mounts.add(mount);
            when(player.hasMount(eq(mountName))).thenReturn(true);
        }
        when(player.getMounts()).thenReturn(mounts);

        playerMap.put(playerName, player);
        visiblePlayers.add(player);
    }

    private void givenNotVisibleMounts() {
        List<Mount> mounts = new ArrayList<>();

        Mount mount1 = mock(Mount.class);
        when(mount1.getId()).thenReturn(1L);
        when(mount1.getName()).thenReturn("m3");
        mounts.add(mount1);

        Mount mount2 = mock(Mount.class);
        when(mount2.getId()).thenReturn(2L);
        when(mount2.getName()).thenReturn("m2");
        mounts.add(mount2);

        Mount mount3 = mock(Mount.class);
        when(mount3.getId()).thenReturn(3L);
        when(mount3.getName()).thenReturn("m1");
        mounts.add(mount3);

        when(mountRepository.findAllByVisible(eq(false))).thenReturn(mounts);
    }

    private void whenGetMounts() {
        mountResponse = playerTracker.getMounts();
    }

    private void whenCleanOldPlayers() {
        playerTracker.cleanOldPlayers();
    }

    private void whenAddMount() throws MountException {
        playerTracker.addMount("test");
    }

    private void whenRemoveMount() throws MountException {
        playerTracker.removeMount(1L);
    }

    private void whenLoadMounts() {
        playerTracker.loadMounts();
    }

    private void whenGetAvailableMounts() {
        availableMounts = playerTracker.getAvailableMounts();
    }

    private void thenLastUpdatedShouldBeSet() {
        String lastUpdated = playerTracker.getLastUpdated();
        assertNotNull(lastUpdated);
        assertThat(lastUpdated, matchesPattern("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}"));
    }

    private void thenPlayerDataShouldBeLoaded() {
        verify(fcLoader, times(1)).loadPlayerData();
    }

    private void thenMountShouldExist() {
        assertEquals(2, mountResponse.size());
        MountRS resultMount = mountResponse.get(1);
        assertNotNull(resultMount);
        assertEquals(3L, resultMount.getId());
        assertEquals("m3", resultMount.getName());

        Map<String, String> players = resultMount.getPlayers();
        assertEquals(2, players.size());
        assertEquals("X", players.get("player-1"));
    }

    private void thenPlayerShouldBeDeleted() {
        verify(playerService, times(1)).deletePlayers(playersCaptor.capture());
        List<Player> deletedPlayers = playersCaptor.getValue();
        assertEquals(1, deletedPlayers.size());
        assertEquals(SYTH, deletedPlayers.get(0).getName());
    }

    private void thenPlayerShouldNotBeDeleted() {
        verify(playerService, times(0)).deletePlayers(any());
    }

    private void thenDeleteShouldNotBeCalled() {
        verify(playerService, times(0)).deletePlayers(anyListOf(Player.class));
    }

    private void thenMountVisibilityShouldBe(boolean visible) {
        verify(mount, times(1)).setVisible(eq(visible));
    }

    private void thenMountShouldBeSaved() {
        verify(mountRepository, times(1)).save(any(Mount.class));
    }

    private void thenFindAllByVisibleShouldBeCalled() {
        verify(mountRepository, times(1)).findAllByVisible(eq(false));
    }

    private void thenThereShouldBeThreeAvailableMounts() {
        assertEquals(3, availableMounts.size());
    }

    private void thenAvailableMountsShouldBeSortedByName() {
        AvailableMount mount1 = availableMounts.get(0);
        assertEquals(3, mount1.getId());
        assertEquals("m1", mount1.getName());

        AvailableMount mount2 = availableMounts.get(1);
        assertEquals(2, mount2.getId());
        assertEquals("m2", mount2.getName());

        AvailableMount mount3 = availableMounts.get(2);
        assertEquals(1, mount3.getId());
        assertEquals("m3", mount3.getName());
    }
}
