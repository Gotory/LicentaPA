package cosmin.licenta;

import org.junit.Test;

import cosmin.licenta.Common.Helper;

import static org.junit.Assert.*;

public class UnitTest {

    @Test
    public void getHelperInstance() {
        Helper helper = Helper.getInstance();
        if(helper != null) {
            assert true;
        } else {
            fail();
        }
    }

}