# Glide - [GitHub](https://github.com/bumptech/glide)

## Kaj je Glide?

Glide je hitra knjižnica za upravljanje slik spisana primarno v Javi. Namenjena je preprostemu nalaganju, obdelavi in prikazovanju slik v Android aplikacijah.

## Prednosti

1. **Hitro nalaganje slik:** Glide omogoča učinkovito nalaganje slik s strežnika/spleta; zmanjša čas nalaganja in izboljša uporabniško izkušnjo.

2. **Enostavna obdelava slik:** Slike je mogoče preprosto obdelati s funkcijami, kot so obrezovanje, spreminjanje velikosti, spreminjanje robov in druge manipulacije.

3. **Predpomnjenje slik:** Knjižnica samodejno upravlja s predpomnilnikom, kar pripomore k hitrejšemu prikazovanju slik in zmanjšanju obremenitve strežnika.

4. **Različni viri slik:** Glide se lahko integrira s različnimi viri slik, vključno z lokalnim pomnilnikom, oddaljenimi strežniki in drugimi virom.

5. **Razumljiva sintaksa:** Uporablja razumljivo in intuitivno sintakso z dobro poimenovanimi funkcijami.
   
## Potenicalne slabosti

1. **Omejeno prilagajanje:** Glide nudi zadovoljivo število funkcionalnosti za urejanje slik, vendar jih je lahko za določene razvijalce premalo. 

2. **Velikost knjižnice:** V primeru, kjer želimo zelo optimizirati velikost aplikacije.

3. **Kompatibinost:** Problemi s kompatibilnostjo za starejše Android verzije.


## Osnovni primer uporabe (MyRecyclerViewAdapterDish)

```kotlin
    Glide.with(holder.itemView)
        .load(itemsViewModel.imgLink)
        .placeholder(R.drawable.dish_placeholder_white)
        .error(R.drawable.dish_placeholder_white)
        .into(holder.imageView)
```

## Primer urejanja slike

```kotlin        
    Glide.with(holder.itemView)
        .load(itemsViewModel.imgLink)
        .placeholder(R.drawable.dish_placeholder_white)
        .error(R.drawable.dish_placeholder_white)
        .apply(
            RequestOptions
            //.centerCropTransform() // Slika je popolnoma vidna brez whitespace-a
            .circleCropTransform() // Izreži sliko kot krog
            //.transform(RoundedCorners(16)) // Dodaj mehke robove
            //.override(300, 300) // Podaj širino in višino
        )
        .into(holder.imageView);
```

## Primer iz aplikacije

<table>
  <tr>
    <td>
      <img alt="Screenshot_1" height="1000px" src="./demo_images/rounded_vendors_screen.jpg" width="450px"/>
    </td>
    <td>
      <img alt="Screenshot_2" height="1000px" src="./demo_images/vendor_dishes_screen.jpg" width="450px"/>
    </td>
  </tr>
</table>

## Razširitve

Funkcije za transformacijo slik lahko razširimo s knjižnico [Glide-Transformations](https://github.com/shanescarlett/Glide-Transformations),
s katero lahko sliki dodamo Padding (direktno v BitMap), Shadow, jo izrežemo v obliki Elipse ipd.

## Podatki

### Avtor: Sam Judd (GitHub - @sjudd)
### Št. razvijalcev: 146
### Zadnja verzija: Glide 5.0.0-rc01; 26. 9. 2023
### GitHub: Watch - 1k, Fork - 6.1k, Star - 34.1k
### Licence: BSD, part MIT and Apache 2.0 [Link](https://github.com/bumptech/glide/blob/master/LICENSE). 
