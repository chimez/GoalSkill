from django.shortcuts import render
from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import get_object_or_404, render
from django.urls import reverse

from .models import Skill, SkillFamily, User, UserMethod, UserSkill


def index(request):
    user = User.objects.get(name="me")
    skill_families = user.skill_family.all()
    context = {
        "skill_families": skill_families,
    }
    return render(request, 'skill_tree/index.html', context)


def skill_family(request, skill_family_name):
    me = User.objects.get(name="me")
    skill_family = SkillFamily.objects.get(name=skill_family_name)
    all_skill = UserSkill.objects.filter(
        user=me, skill__skill_family=skill_family)
    context = {
        "all_skill": all_skill,
        "skill_family_name": skill_family_name,
    }
    return render(request, 'skill_tree/skill_family.html', context)


def skill(request, skill_family_name, skill_name):
    me = User.objects.get(name="me")
    skill = Skill.objects.get(name=skill_name)
    skill_exp = UserSkill.objects.get(user=me, skill=skill).skill_exp
    level = UserSkill.objects.get(user=me, skill=skill).skill_level
    methods_obj = UserMethod.objects.filter(user=me, skill=skill)
    methods = []
    for method in methods_obj:
        method_need_times = (level.need_exp - skill_exp) / method.method.exp
        methods.append({
            "method": method,
            "method_need_times": "%0.f" % method_need_times,
        })

    context = {
        "skill": skill,
        "level": level,
        "skill_exp": skill_exp,
        "methods": methods,
        "skill_family_name": skill_family_name,
        "skill_name": skill.name,
    }
    return render(request, 'skill_tree/skill.html', context)


def inc_method_times(request, skill_family_name, skill_name, user_method_id):
    me = User.objects.get(name="me")
    user_method = get_object_or_404(UserMethod, pk=user_method_id)
    user_method.method_times += 1
    user_method.save()
    user_skill = UserSkill.objects.get(user=me, skill__name=skill_name)
    user_skill.skill_exp += user_method.method.exp
    user_skill.save()
    return HttpResponseRedirect(
        reverse('skill_tree:skill', args=(skill_family_name, skill_name)))
