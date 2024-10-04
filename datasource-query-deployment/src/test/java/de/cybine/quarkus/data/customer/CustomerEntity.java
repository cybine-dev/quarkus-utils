package de.cybine.quarkus.data.customer;

import de.cybine.quarkus.data.rental.*;
import de.cybine.quarkus.util.*;
import jakarta.persistence.*;
import lombok.*;

import java.io.*;
import java.util.*;

@Data
@NoArgsConstructor
@Table(name = CustomerEntity_.TABLE)
@Entity(name = CustomerEntity_.ENTITY)
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomerEntity implements Serializable, WithId<Long>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = CustomerEntity_.ID_COLUMN)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = CustomerEntity_.FIRSTNAME_COLUMN, nullable = false)
    private String firstname;

    @Column(name = CustomerEntity_.LASTNAME_COLUMN, nullable = false)
    private String lastname;

    @OneToMany(mappedBy = RentalEntity_.CUSTOMER_RELATION)
    private List<RentalEntity> rentals;

    public Optional<List<RentalEntity>> getRentals( )
    {
        return Optional.ofNullable(this.rentals);
    }

    @Override
    public boolean equals(Object other)
    {
        // @formatter:off
        if (other == null)
            return false;

        if (this.getClass() != other.getClass())
            return false;

        WithId<?> that = (WithId<?>) other;
        if (this.findId().isEmpty() || that.findId().isEmpty())
            return false;

        return Objects.equals(this.getId(), that.getId());
        // @formatter:on
    }

    @Override
    public int hashCode( )
    {
        return this.findId().map(Object::hashCode).orElse(0);
    }
}