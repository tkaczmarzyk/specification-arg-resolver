package net.kaczmarzyk.spring.data.jpa.domain;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.Gender;
import net.kaczmarzyk.spring.data.jpa.IntegrationTestBase;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Locale;

import static net.kaczmarzyk.spring.data.jpa.CustomerBuilder.customer;
import static org.assertj.core.api.Assertions.assertThat;

public class NotLikeIgnoreCaseLocaleIntegrationTest extends IntegrationTestBase {
    private Customer homerWithLowercaseI;
    private Customer homerWithEnglishCapitalI;
    private Customer homerWithTurkishCapitalI;

    @Before
    public void initData() {
        homerWithLowercaseI = customer("Homer", "Simpson").gender(Gender.MALE).build(em);
        homerWithEnglishCapitalI = customer("Homer", "SIMPSON").gender(Gender.MALE).build(em);
        homerWithTurkishCapitalI = customer("Homer", "SİMPSON").build(em);
    }

    @Test
    public void usesLocaleWhenPerformingComparisons() {

        // English locale
        NotLikeIgnoreCase<Customer> simpsons = new NotLikeIgnoreCase<>(queryCtx, "lastName", new String[] { "i" });
        simpsons.setLocale(Locale.ENGLISH);
        List<Customer> simpsonsFound = customerRepo.findAll(simpsons);

        assertThat(simpsonsFound)
                .hasSize(1)
                .containsOnly(homerWithTurkishCapitalI);

        // Turkish locale
        simpsons = new NotLikeIgnoreCase<>(queryCtx, "lastName", new String[] { "i" });
        simpsons.setLocale(new Locale("tr", "TR"));
        simpsonsFound = customerRepo.findAll(simpsons);

        assertThat(simpsonsFound)
                .hasSize(2)
                .containsOnly(homerWithLowercaseI, homerWithEnglishCapitalI);
    }
}