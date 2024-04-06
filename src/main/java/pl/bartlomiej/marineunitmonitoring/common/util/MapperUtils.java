package pl.bartlomiej.marineunitmonitoring.common.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.BeanUtils;

import static java.util.Arrays.stream;

@Slf4j
public class MapperUtils {

    @SneakyThrows
    public static <T> T copyProperties(Object source, Class<T> targetClass) {
        T target = targetClass.getDeclaredConstructor().newInstance();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static Document objectToDocument(Object object) {
        return stream(object.getClass().getDeclaredFields())
                .peek(field -> field.setAccessible(true))
                .collect(
                        Document::new,
                        (document, field) -> {
                            try {
                                document.append(field.getName(), field.get(object));
                            } catch (IllegalAccessException e) {
                                log.error("Error from objToDoc mapping: {}", e.getMessage());
                            }
                        },
                        Document::putAll
                );
    }
}
