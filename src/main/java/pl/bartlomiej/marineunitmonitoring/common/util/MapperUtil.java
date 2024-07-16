package pl.bartlomiej.marineunitmonitoring.common.util;

import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;

final public class MapperUtil {

    private MapperUtil() {
    }

    @SneakyThrows
    public static <T> T copyProperties(Object source, Class<T> targetClass) {
        T target = targetClass.getDeclaredConstructor().newInstance();
        BeanUtils.copyProperties(source, target);
        return target;
    }
}
