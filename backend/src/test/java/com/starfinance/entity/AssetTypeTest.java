package com.starfinance.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AssetTypeTest {

    @Test
    void fromPurity_SupportedKarats() {
        assertEquals(Asset.AssetType.TWENTY_FOUR_CARAT, Asset.AssetType.fromPurity("24K"));
        assertEquals(Asset.AssetType.TWENTY_FOUR_CARAT, Asset.AssetType.fromPurity("24 Karat"));
        assertEquals(Asset.AssetType.TWENTY_TWO_CARAT, Asset.AssetType.fromPurity("22K"));
        assertEquals(Asset.AssetType.EIGHTEEN_CARAT, Asset.AssetType.fromPurity("18K"));
        assertEquals(Asset.AssetType.SIXTEEN_CARAT, Asset.AssetType.fromPurity("16K"));
        assertEquals(Asset.AssetType.FOURTEEN_CARAT, Asset.AssetType.fromPurity("14K"));
        assertEquals(Asset.AssetType.FOURTEEN_CARAT, Asset.AssetType.fromPurity("14 Karat"));
        assertEquals(Asset.AssetType.EIGHT_CARAT, Asset.AssetType.fromPurity("8K"));
    }

    @Test
    void fromPurity_InvalidPurity_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Asset.AssetType.fromPurity("10K"));
        assertThrows(IllegalArgumentException.class, () -> Asset.AssetType.fromPurity(""));
    }

    @Test
    void fromDbValue_Success() {
        assertEquals(Asset.AssetType.FOURTEEN_CARAT, Asset.AssetType.fromDbValue("14 Carat"));
        assertEquals(Asset.AssetType.TWENTY_FOUR_CARAT, Asset.AssetType.fromDbValue("24 Carat"));
    }
}
