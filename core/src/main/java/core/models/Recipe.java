package core.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Recipe {

	private String name;
	
	private String description;

	private double unitPrice;
}
