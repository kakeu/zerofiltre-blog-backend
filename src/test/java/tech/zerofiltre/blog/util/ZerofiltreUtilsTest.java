package tech.zerofiltre.blog.util;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ZerofiltreUtilsTest {

    @Test
    void defineStartDateAndEndDate() {
        //ARRANGE
        //ACT
        List<LocalDate> list = ZerofiltreUtils.defineStartDateAndEndDate();

        //ASSERT
        assertThat(list.get(0)).isBefore(list.get(1));
    }

    @Test
    void partitionList() {
        //ARRANGE
        List<Integer> listInteger = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 ,15);
        int partitionSize = 4;

        //ACT
        Collection<List<Integer>> list = ZerofiltreUtils.partitionList(listInteger, partitionSize);

        //ASSERT
        assertThat(list.size()).isEqualTo(4);
        assertThat(list.contains(Arrays.asList(1, 2, 3, 4))).isTrue();
        assertThat(list.contains(Arrays.asList(5, 6, 7, 8))).isTrue();
        assertThat(list.contains(Arrays.asList(9, 10, 11, 12))).isTrue();
        assertThat(list.contains(Arrays.asList(13, 14 ,15))).isTrue();
        assertThat(list.contains(Arrays.asList(13, 14 ,15, 16))).isFalse();
    }

    @Test
    void getRootCauseMessageForException() {
        //ARRANGE
        Exception exception1 = new Exception("exception1", null);
        Exception exception2 = new Exception("exception2", exception1);
        Exception exception3 = new Exception("exception3", exception2);
        Exception exception4 = new Exception("exception4", exception3);

        //ACT
        String rootCause = ZerofiltreUtils.getRootCauseMessage(exception4);

        //ASSERT
        AssertionsForClassTypes.assertThat(rootCause).isEqualTo("exception1");
    }

    @Test
    void sanitizeFilename() {
        //ARRANGE
        List<String> fileNamesList = Arrays.asList("a&&a", "bééb", "c//c", "d\\\\d", "e::e", "f**f", "g??g", "h\"\"h", "i<<i", "j>>j", "k||k", "l  l");
        List<String> expectedFileNamesList = Arrays.asList("a&&a", "bééb", "c_c", "d_d", "e_e", "f_f", "g_g", "h_h", "i_i", "j_j", "k_k", "l_l");

        //ACT
        List<String> responses = new ArrayList<>();
        for (String s : fileNamesList) {
            responses.add(ZerofiltreUtils.sanitizeString(s));
        }

        //ASSERT
        AssertionsForClassTypes.assertThat(responses).isEqualTo(expectedFileNamesList);
    }
}