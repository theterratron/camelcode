package controllers;

import com.google.common.base.CharMatcher;
import models.PostcodeUnit;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import java.util.ArrayList;
import java.util.List;

import static play.libs.Json.toJson;

public class Application extends Controller {
    public static class DistanceCalc {
        @Constraints.Required
        public String postcode;

        @Constraints.Required
        public int distance = 10;

        public String validate() {
            if (PostcodeUnit.find.field("postcode").equal(postcode).get() == null) {
                return "Invalid postcode";
            }

            return null;
        }
    }

    public static class Geocode {
        @Constraints.Required
        public String postcode;

        public String validate() {
            if (PostcodeUnit.find.field("postcode").equal(postcode).get() == null) {
                return "Invalid postcode";
            }

            return null;
        }
    }

    public static Result index() {
        return ok(index.render(form(DistanceCalc.class), form(Geocode.class), new ArrayList<PostcodeUnit>()));
    }

    public static Result ll() {
        Form<Geocode> geocodeForm = form(Geocode.class).bindFromRequest();
        if (geocodeForm.hasErrors()) {
            return badRequest(index.render(form(DistanceCalc.class), geocodeForm, new ArrayList<PostcodeUnit>()));
        } else {
            Geocode geocode = geocodeForm.get();

            PostcodeUnit unit = PostcodeUnit.find.field("postcode").equal(CharMatcher.WHITESPACE.removeFrom(geocode.postcode)).get();
            return ok(toJson(unit.location));
        }
    }

    public static Result latLng(String postcode) {
        if (postcode == null) return notFound();

        PostcodeUnit unit = PostcodeUnit.find.field("postcode").equal(CharMatcher.WHITESPACE.removeFrom(postcode.toUpperCase())).get();
        if (unit == null) {
            return notFound();
        } else {
            return ok(toJson(unit.location));
        }
    }

    public static Result en() {
        Form<Geocode> geocodeForm = form(Geocode.class).bindFromRequest();
        if (geocodeForm.hasErrors()) {
            return badRequest(index.render(form(DistanceCalc.class), geocodeForm, new ArrayList<PostcodeUnit>()));
        } else {
            Geocode geocode = geocodeForm.get();

            PostcodeUnit unit = PostcodeUnit.find.field("postcode").equal(CharMatcher.WHITESPACE.removeFrom(geocode.postcode)).get();
            return ok(toJson(unit.cartesianLocation));
        }
    }

    public static Result eastingsNorthings(String postcode) {
        if (postcode == null) return notFound();

        PostcodeUnit unit = PostcodeUnit.find.field("postcode").equal(CharMatcher.WHITESPACE.removeFrom(postcode.toUpperCase())).get();
        if (unit == null) {
            return notFound();
        } else {
            return ok(toJson(unit.cartesianLocation));
        }
    }

    public static Result calc() {
        Form<DistanceCalc> distanceCalcForm = form(DistanceCalc.class).bindFromRequest();
        if (distanceCalcForm.hasErrors()) {
            return badRequest(index.render(distanceCalcForm, form(Geocode.class), new ArrayList<PostcodeUnit>()));
        } else {
            DistanceCalc calc = distanceCalcForm.get();

            PostcodeUnit postcode = PostcodeUnit.find.field("postcode").equal(CharMatcher.WHITESPACE.removeFrom(calc.postcode)).get();
            List<PostcodeUnit> near = PostcodeUnit.find.field("location")
                    .near(postcode.location.latitude, postcode.location.longitude)
                    .limit(10)
                    .asList();

            flash("success", "Found " + near.size() + " post codes within " + calc.distance + " miles from " + postcode.postcode);

            return ok(index.render(distanceCalcForm, form(Geocode.class), near));
        }
    }
}