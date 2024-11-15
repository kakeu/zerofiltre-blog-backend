package tech.zerofiltre.blog.domain.tips;

import tech.zerofiltre.blog.domain.error.ZerofiltreException;

import java.util.Locale;

public interface TipsProvider {

    String getTip(String profession, Locale locale) throws ZerofiltreException;
}
