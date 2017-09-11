package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.tasks.release.VersionUpgradeStrategyFactory
import org.gradle.api.GradleException
import org.junit.Assert
import org.junit.Test

class VersionUpgradeStrategyFactoryTest {

    private static final String SUFFIX = '-SNAPSHOT'

    @Test
    void doesParseVersionInfo() {
        def info = VersionUpgradeStrategyFactory.parseVersionInfo("1.2.3")
        Assert.assertEquals(1, info.major)
        Assert.assertEquals(2, info.minor)
        Assert.assertEquals(3, info.patch)
    }

    @Test(expected = GradleException)
    void doesParseVersionInfo_wrongPattern() {
        VersionUpgradeStrategyFactory.parseVersionInfo("42")
    }

    @Test
    void doesCreateMajorVersionUpgradeStrategy() {
        def strategy = VersionUpgradeStrategyFactory.createMajorVersionUpgradeStrategy(SUFFIX);
        Assert.assertEquals("2.0.0", strategy.getVersion("1.2.3-SNAPSHOT"))
    }

    @Test
    void doesCreateMinorVersionUpgradeStrategy() {
        def strategy = VersionUpgradeStrategyFactory.createMinorVersionUpgradeStrategy(SUFFIX);
        Assert.assertEquals("1.3.0", strategy.getVersion("1.2.3-SNAPSHOT"))
    }

    @Test
    void doesCreatePatchVersionUpgradeStrategy() {
        def strategy = VersionUpgradeStrategyFactory.createPatchVersionUpgradeStrategy(SUFFIX);
        Assert.assertEquals("1.2.3", strategy.getVersion("1.2.3-SNAPSHOT"))
    }

    @Test
    void doesCreateSnapshotVersionUpgradeStrategy() {
        def strategy = VersionUpgradeStrategyFactory.createSnapshotVersionUpgradeStrategy();
        Assert.assertEquals("1.2.3-SNAPSHOT", strategy.getVersion("1.2.3-SNAPSHOT"))
    }

}
