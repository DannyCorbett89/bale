package com.dc.bale.service;

import com.dc.bale.database.dao.MountLinkRepository;
import com.dc.bale.database.dao.TrialRepository;
import com.dc.bale.database.entity.Mount;
import com.dc.bale.database.entity.MountLink;
import com.dc.bale.database.entity.Trial;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TrialServiceTest {
    private TrialRepository trialRepository = mock(TrialRepository.class);
    private MountLinkRepository mountLinkRepository = mock(MountLinkRepository.class);

    private TrialService trialService = new TrialService(trialRepository, mountLinkRepository);

    private Mount mount;
    private String instance;
    private Map<Long, Long> mountItemLevels;

    @Test
    public void testGetInstance_NullMountName() {
        givenMountWithNullName();
        whenGetInstance();
        thenInstanceShouldBeNull();
    }

    @Test
    public void testGetInstance_ZeroTrialBossNames() {
        givenMount();
        whenGetInstance();
        thenInstanceShouldBe("m1");
    }

    @Test
    public void testGetInstance_OneTrialBossName() {
        givenMountLinks(1);
        givenTrial();
        givenMount();
        whenGetInstance();
        thenInstanceShouldBe("b1");
    }

    @Test
    public void testGetInstance_TwoTrialBossNames() {
        givenMountLinks(2);
        givenTrial();
        givenMount();
        whenGetInstance();
        thenInstanceShouldBe("m1");
    }

    @Test
    public void testGetMountItemLevels() {
        givenMountLinks();
        givenTrial();
        whenGetMountItemLevels();
        thenItemLevelsShouldBePopulated();
    }

    @Test
    public void testGetMountItemLevels_DuplicateMountIDs() {
        givenDuplicateMountLinks();
        givenTrial();
        whenGetMountItemLevels();
        thenFirstDuplicateShouldBeChosen();
    }

    private void givenTrial() {
        Trial trial1 = mock(Trial.class);
        when(trial1.getItemLevel()).thenReturn(11L);
        when(trial1.getBoss()).thenReturn("b1");

        Trial trial2 = mock(Trial.class);
        when(trial2.getItemLevel()).thenReturn(22L);
        when(trial2.getBoss()).thenReturn("b2");

        when(trialRepository.findOne(anyLong())).thenReturn(trial1, trial2, null);
    }

    private void givenMountLinks(int numLinks) {
        List<MountLink> mountLinks = new ArrayList<>();
        for (int x = 0; x < numLinks; x++) {
            mountLinks.add(mock(MountLink.class));
        }
        when(mountLinkRepository.findAllByMountIdAndTrialIdGreaterThan(anyLong(), anyLong())).thenReturn(mountLinks);
    }

    private void givenMountLinks() {
        List<MountLink> mountLinks = new ArrayList<>();

        MountLink mountLink1 = mock(MountLink.class);
        when(mountLink1.getMountId()).thenReturn(1L);
        when(mountLink1.getTrialId()).thenReturn(11L);
        mountLinks.add(mountLink1);

        MountLink mountLink2 = mock(MountLink.class);
        when(mountLink2.getMountId()).thenReturn(2L);
        when(mountLink2.getTrialId()).thenReturn(22L);
        mountLinks.add(mountLink2);

        MountLink mountLink3 = mock(MountLink.class);
        when(mountLink3.getMountId()).thenReturn(3L);
        mountLinks.add(mountLink3);

        MountLink mountLink4 = mock(MountLink.class);
        when(mountLink4.getMountId()).thenReturn(4L);
        when(mountLink4.getTrialId()).thenReturn(44L);
        mountLinks.add(mountLink4);

        when(mountLinkRepository.findAll()).thenReturn(mountLinks);
    }

    private void givenDuplicateMountLinks() {
        List<MountLink> mountLinks = new ArrayList<>();

        MountLink mountLink1 = mock(MountLink.class);
        when(mountLink1.getMountId()).thenReturn(1L);
        when(mountLink1.getTrialId()).thenReturn(11L);
        mountLinks.add(mountLink1);

        MountLink mountLink2 = mock(MountLink.class);
        when(mountLink2.getMountId()).thenReturn(1L);
        when(mountLink2.getTrialId()).thenReturn(22L);
        mountLinks.add(mountLink2);

        when(mountLinkRepository.findAll()).thenReturn(mountLinks);
    }

    private void givenMountWithNullName() {
        mount = mock(Mount.class);
    }

    private void givenMount() {
        mount = mock(Mount.class);
        when(mount.getName()).thenReturn("m1");
    }

    private void whenGetInstance() {
        instance = trialService.getInstance(mount);
    }

    private void whenGetMountItemLevels() {
        mountItemLevels = trialService.getMountItemLevels();
    }

    private void thenInstanceShouldBeNull() {
        assertNull(instance);
    }

    private void thenInstanceShouldBe(String instance) {
        assertEquals(instance, this.instance);
    }

    private void thenItemLevelsShouldBePopulated() {
        assertEquals(3, mountItemLevels.size());
        assertEquals(Long.valueOf(11), mountItemLevels.get(1L));
        assertEquals(Long.valueOf(22), mountItemLevels.get(2L));
        assertEquals(Long.valueOf(0), mountItemLevels.get(4L));
    }

    private void thenFirstDuplicateShouldBeChosen() {
        assertEquals(1, mountItemLevels.size());
        assertEquals(Long.valueOf(11), mountItemLevels.get(1L));
    }
}
